package dev.su5ed.somnia.util;

import dev.su5ed.somnia.SomniaConfig;
import dev.su5ed.somnia.acceleration.AccelerationHandler;
import dev.su5ed.somnia.acceleration.AccelerationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

@SuppressWarnings("unused")
public class InjectHooks {
    public static boolean doMobSpawning(ServerLevel level) {
        return level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
            && (!SomniaConfig.COMMON.disableCreatureSpawning.get() || !isActiveTickHandler(level));
    }

    private static boolean isActiveTickHandler(ServerLevel level) {
        return AccelerationManager.HANDLERS.stream()
            .filter(handler -> handler.level == level)
            .map(AccelerationHandler::isActive)
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Couldn't find tick handler for given world"));
    }
}
