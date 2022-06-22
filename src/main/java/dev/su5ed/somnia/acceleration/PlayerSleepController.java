package dev.su5ed.somnia.acceleration;

import dev.su5ed.somnia.Somnia;
import dev.su5ed.somnia.SomniaConfig;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.IFatigue;
import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.compat.DarkUtilsPlugin;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.PlayerWakeUpPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Somnia.MODID)
public final class PlayerSleepController {

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
