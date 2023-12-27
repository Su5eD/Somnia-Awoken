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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSleepInBedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.SleepingTimeCheckEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = SomniaAwoken.MODID)
public final class PlayerSleepController {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSleepingTimeCheck(SleepingTimeCheckEvent event) {
        Player player = event.getEntity();
        if (!DarkUtilsCompat.hasSleepCharm(player) && !Optional.ofNullable(player.getCapability(CapabilityFatigue.INSTANCE)).map(Fatigue::shouldSleepNormally).orElse(false)) {
            event.setResult(!SomniaUtil.isEnterSleepTime(player.level()) ? Event.Result.DENY : Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (!SomniaUtil.checkFatigue(player)) {
            player.displayClientMessage(Component.translatable("somnia.status.cooldown"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
        else if (!SomniaConfig.COMMON.sleepWithArmor.get() && !player.isCreative() && SomniaUtil.hasArmor(player)) {
            player.displayClientMessage(Component.translatable("somnia.status.armor"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }

        Fatigue fatigue = player.getCapability(CapabilityFatigue.INSTANCE);
        if (fatigue != null) {
            fatigue.setSleepNormally(player.isShiftKeyDown());
        }

        SomniaUtil.updateWakeTime((ServerPlayer) player);
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        LevelAccessor level = event.getLevel();

        if (SomniaConfig.COMMON.enableFatigue.get()) {
            level.players().stream()
                .filter(Player::isSleepingLongEnough)
                .forEach(player -> Optional.ofNullable(player.getCapability(CapabilityFatigue.INSTANCE))
                    .filter(props -> props.shouldSleepNormally() || DarkUtilsCompat.hasSleepCharm(player))
                    .ifPresent(props -> {
                        long timeSlept = event.getNewTime() - level.dayTime();
                        double replenish = SomniaConfig.COMMON.fatigueReplenishRate.get() * timeSlept;
                        props.setFatigue(props.getFatigue() - replenish);
                    }));
        }
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        Fatigue fatigue = player.getCapability(CapabilityFatigue.INSTANCE);
        if (fatigue != null) {
            fatigue.maxFatigueCounter();
            fatigue.setResetSpawn(true);
            fatigue.setSleepNormally(false);
            fatigue.setSleepOverride(false);
            fatigue.setWakeTime(-1);
        }
    }

    @SubscribeEvent
    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        Fatigue fatigue = event.getEntity().getCapability(CapabilityFatigue.INSTANCE);
        if (fatigue != null && !fatigue.getResetSpawn()) {
            event.setCanceled(true);
        }
    }

    // we need the earliest PlayerEntity#hurt listener
    // because we have to set the sleep override to false before the mc stopSleeping call
    // otherwise PlayerSleepTickHandler#tickEnd will make the player to start sleeping again
    @SubscribeEvent
    public static void onPlayerDamage(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof ServerPlayer player && entity.isSleeping()) {
            if (player.isInvulnerableTo(event.getSource())
                || player.isInvulnerable() && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || player.isOnFire() && player.hasEffect(MobEffects.FIRE_RESISTANCE)
            ) {
                return;
            }

            Fatigue fatigue = entity.getCapability(CapabilityFatigue.INSTANCE);
            if (fatigue != null) {
                fatigue.setSleepOverride(false);
            }
            entity.stopSleeping();
            SomniaNetwork.sendToClient(new PlayerWakeUpPacket(), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            Fatigue fatigue = serverPlayer.getCapability(CapabilityFatigue.INSTANCE);
            if (fatigue != null) {
                if (event.phase == TickEvent.Phase.START) playerTickStart(fatigue, serverPlayer);
                else playerTickEnd(fatigue, serverPlayer);
            }
        }
    }

    private static void playerTickStart(Fatigue fatigue, Player player) {
        if (player.isSleeping() && player.checkBedExists()) {
            if (fatigue.shouldSleepNormally() || player.getSleepTimer() >= 90 && DarkUtilsCompat.hasSleepCharm(player) || Compat.isSleepingInHammock(player)) {
                fatigue.setSleepOverride(false);
            }
            else {
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
        if (wakeTime != -1 && (player.level().getGameTime() >= wakeTime || fatigue.getFatigue() == 0 && SomniaConfig.COMMON.forceWakeUp.get() && !player.isCreative())) {
            player.stopSleepInBed(true, true);
            SomniaNetwork.sendToClient(new PlayerWakeUpPacket(), player);
        }
        else if (fatigue.sleepOverride()) {
            fatigue.setSleepOverride(false);

            player.startSleeping(player.getSleepingPos().orElse(player.blockPosition()));
        }
    }

    private PlayerSleepController() {}
}
