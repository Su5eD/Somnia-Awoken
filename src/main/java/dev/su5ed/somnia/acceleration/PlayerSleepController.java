package dev.su5ed.somnia.acceleration;

import dev.su5ed.somnia.SomniaAwoken;
import dev.su5ed.somnia.SomniaConfig;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.Fatigue;
import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.compat.DarkUtilsCompat;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.PlayerWakeUpPacket;
import dev.su5ed.somnia.util.SomniaUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SomniaAwoken.MODID)
public final class PlayerSleepController {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSleepingTimeCheck(SleepingTimeCheckEvent event) {
        Player player = event.getEntity();
        if (DarkUtilsCompat.hasSleepCharm(player) || player.getCapability(CapabilityFatigue.INSTANCE).map(Fatigue::shouldSleepNormally).orElse(false)) return;

        if (!SomniaUtil.isEnterSleepTime(player.level)) event.setResult(Event.Result.DENY);
        else event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (!SomniaUtil.checkFatigue(player)) {
            player.displayClientMessage(Component.translatable("somnia.status.cooldown"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        } else if (!SomniaConfig.COMMON.sleepWithArmor.get() && !player.isCreative() && SomniaUtil.hasArmor(player)) {
            player.displayClientMessage(Component.translatable("somnia.status.armor"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }

        player.getCapability(CapabilityFatigue.INSTANCE)
            .ifPresent(props -> props.setSleepNormally(player.isShiftKeyDown()));

        SomniaUtil.updateWakeTime((ServerPlayer) player);
    }
    
    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        LevelAccessor level = event.getLevel();
        
        level.players().stream()
            .filter(Player::isSleepingLongEnough)
            .forEach(player -> player.getCapability(CapabilityFatigue.INSTANCE)
                .filter(props -> props.shouldSleepNormally() || DarkUtilsCompat.hasSleepCharm(player))
                .ifPresent(props -> {
                    long timeSlept = event.getNewTime() - level.dayTime();
                    double replenish = SomniaConfig.COMMON.fatigueReplenishRate.get() * timeSlept;
                    props.setFatigue(props.getFatigue() - replenish);
                }));
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> {
            props.maxFatigueCounter();
            props.setResetSpawn(true);
            props.setSleepNormally(false);
            props.setSleepOverride(false);
            props.setWakeTime(-1);
        });
    }

    @SubscribeEvent
    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        event.getEntity().getCapability(CapabilityFatigue.INSTANCE)
            .map(Fatigue::getResetSpawn)
            .ifPresent(resetSpawn -> {
                if (!resetSpawn) event.setCanceled(true);
            });
    }
    
    // we need the earliest PlayerEntity#hurt listener
    // because we have to set the sleep override to false before the mc stopSleeping call
    // otherwise PlayerSleepTickHandler#tickEnd will make the player to start sleeping again
    @SubscribeEvent
    public static void onPlayerDamage(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof ServerPlayer player && entity.isSleeping()) {
            if (player.isInvulnerableTo(event.getSource())
                || player.isInvulnerable() && !event.getSource().isBypassInvul()
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

    private static void playerTickStart(Fatigue fatigue, Player player) {
        if (player.isSleeping() && player.checkBedExists()) {
            if (fatigue.shouldSleepNormally() || player.getSleepTimer() >= 90 && DarkUtilsCompat.hasSleepCharm(player) || Compat.isSleepingInHammock(player)) {
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

    private static void playerTickEnd(Fatigue fatigue, ServerPlayer player) {
        long wakeTime = fatigue.getWakeTime();
        if (wakeTime != -1 && player.level.getGameTime() >= wakeTime || fatigue.getFatigue() == 0 && SomniaConfig.COMMON.forceWakeUp.get()) {
            player.stopSleepInBed(true, true);
            SomniaNetwork.sendToClient(new PlayerWakeUpPacket(), player);
        } else if (fatigue.sleepOverride()) {
            fatigue.setSleepOverride(false);

            player.startSleeping(player.getSleepingPos().orElse(player.blockPosition()));
        }
    }

    private PlayerSleepController() {}
}
