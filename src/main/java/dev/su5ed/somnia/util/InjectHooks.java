package dev.su5ed.somnia.util;

import dev.su5ed.somnia.acceleration.AccelerationManager;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.SomniaConfig;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.ClientWakeTimeUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

@SuppressWarnings("unused")
public class InjectHooks {
    public static boolean doMobSpawning(ServerLevel level) {
        return level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
            && (!SomniaConfig.COMMON.disableCreatureSpawning.get() || isActiveTickHandler(level));
    }
    
    private static boolean isActiveTickHandler(ServerLevel level) {
        return AccelerationManager.HANDLERS.stream()
            .filter(handler -> handler.level == level)
            .map(handler -> !handler.isActive())
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Couldn't find tick handler for given world"));
    }

    public static void updateWakeTime(Player player) {
        player.getCapability(CapabilityFatigue.INSTANCE)
            .filter(props -> props.getWakeTime() < 0)
            .ifPresent(props -> {
                long dayTime = SomniaUtil.getLevelDayTime(player.level);
                long wakeTime = SomniaUtil.calculateWakeTime(player.level.getGameTime(), dayTime, dayTime > 12000 ? 0 : 12000);
                props.setWakeTime(wakeTime);
                SomniaNetwork.sendToClient(new ClientWakeTimeUpdatePacket(wakeTime), (ServerPlayer) player);
            });
    }
}
