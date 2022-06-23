package dev.su5ed.somnia.acceleration;

import dev.su5ed.somnia.Somnia;
import dev.su5ed.somnia.SomniaConfig;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.IFatigue;
import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.compat.DarkUtilsPlugin;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.PlayerWakeUpPacket;
import dev.su5ed.somnia.util.InjectHooks;
import dev.su5ed.somnia.util.SomniaUtil;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Somnia.MODID)
public final class PlayerSleepController {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSleepingTimeCheck(SleepingTimeCheckEvent event) {
        Player player = event.getPlayer();
        if (Compat.darkUtilsLoaded && DarkUtilsPlugin.hasSleepCharm(player)
            || player.getCapability(CapabilityFatigue.INSTANCE).map(IFatigue::shouldSleepNormally).orElse(false)) return;

        if (!SomniaUtil.isEnterSleepTime()) event.setResult(Event.Result.DENY);
        else event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        Player player = event.getPlayer();
        if (!SomniaUtil.checkFatigue(player)) {
            player.displayClientMessage(new TranslatableComponent("somnia.status.cooldown"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        } else if (!SomniaConfig.COMMON.sleepWithArmor.get() && !player.isCreative() && SomniaUtil.hasArmor(player)) {
            player.displayClientMessage(new TranslatableComponent("somnia.status.armor"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }

        player.getCapability(CapabilityFatigue.INSTANCE)
            .ifPresent(props -> props.setSleepNormally(player.isShiftKeyDown()));

        if (Compat.isSleepingInBag(player)) InjectHooks.updateWakeTime((ServerPlayer) player);
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getPlayer();
        player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> {
            if (props.shouldSleepNormally() || (Compat.darkUtilsLoaded && DarkUtilsPlugin.hasSleepCharm(player))) {
                props.setFatigue(props.getFatigue() - SomniaUtil.getFatigueToReplenish(player));
            }
            props.maxFatigueCounter();
            props.setResetSpawn(true);
            props.setSleepNormally(false);
            props.setSleepOverride(false);
            props.setWakeTime(-1);
        });
    }

    @SubscribeEvent
    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        event.getPlayer().getCapability(CapabilityFatigue.INSTANCE)
            .map(IFatigue::getResetSpawn)
            .ifPresent(resetSpawn -> {
                if (!resetSpawn) event.setCanceled(true);
            });
    }
    
    // we need the earliest PlayerEntity#hurt listener
    // because we have to set the sleep override to false before the mc stopSleeping call
    // otherwise PlayerSleepTickHandler#tickEnd will make the player to start sleeping again
    @SubscribeEvent
    public static void onPlayerDamage(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (entity instanceof ServerPlayer player && entity.isSleeping()) {
            if (player.isInvulnerableTo(event.getSource())
                || (player.isInvulnerable() && !event.getSource().isBypassInvul())
                || player.isOnFire() && player.hasEffect(MobEffects.FIRE_RESISTANCE)
            ) {
                return;
            }

            entity.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> props.setSleepOverride(false));
            entity.stopSleeping();
            SomniaNetwork.sendToClient(new PlayerWakeUpPacket(), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(CapabilityFatigue.INSTANCE)
                .ifPresent(fatigue -> {
                    if (event.phase == TickEvent.Phase.START) playerTickStart(fatigue, serverPlayer);
                    else playerTickEnd(fatigue, serverPlayer);
                });
        }
    }

    private static void playerTickStart(IFatigue fatigue, Player player) {
        if (player.isSleeping()) {
            if (fatigue.shouldSleepNormally() || (player.getSleepTimer() > 99 && Compat.darkUtilsLoaded && DarkUtilsPlugin.hasSleepCharm(player)) || Compat.isSleepingInHammock(player)) {
                fatigue.setSleepOverride(false);
            } else {
                fatigue.setSleepOverride(true);

                if (SomniaConfig.COMMON.fading.get()) {
                    int sleepTimer = player.getSleepTimer() + 1;
                    if (sleepTimer >= 99) sleepTimer = 98;
                    player.sleepCounter = sleepTimer;
                }
            }
        }
    }

    private static void playerTickEnd(IFatigue fatigue, ServerPlayer player) {
        long wakeTime = fatigue.getWakeTime();
        if (wakeTime != -1 && player.level.getGameTime() >= wakeTime) {
            player.stopSleepInBed(true, true);
            SomniaNetwork.sendToClient(new PlayerWakeUpPacket(), player);
        } else if (fatigue.sleepOverride()) {
            fatigue.setSleepOverride(false);

            player.startSleeping(player.getSleepingPos().orElse(player.blockPosition()));
        }
    }

    private PlayerSleepController() {}
}
