package dev.su5ed.somnia.util;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.SomniaConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class SomniaUtil {
    
    public static boolean hasArmor(Player player) {
        return player.getInventory().armor.stream()
            .anyMatch(stack -> !stack.isEmpty());
    }
    
    public static int getLevelDayTime(Level level) {
        return (int) (level.getDayTime() % 24000L);
    }
    
    public static long calculateWakeTime(Level level, int target) {
        return calculateWakeTime(level.getGameTime(), getLevelDayTime(level), target);
    }

    public static long calculateWakeTime(long gameTime, long dayTime, int target) {
        long wakeTime = gameTime - dayTime + target;
        return dayTime > target ? wakeTime + 24000L : wakeTime;
    }

    public static boolean checkFatigue(Player player) {
        return player.getCapability(CapabilityFatigue.INSTANCE)
            .map(props -> player.isCreative() || props.getFatigue() >= SomniaConfig.COMMON.minimumFatigueToSleep.get())
            .orElse(false);
    }

    public static String timeStringForGameTime(long gameTime) {
        long time = (gameTime + 6000) % 24000;
        String hours = String.valueOf(time / 1000);
        String minutes = String.valueOf((int) ((time % 1000) / 1000D * 60));

        if (hours.length() == 1) hours = "0" + hours;
        if (minutes.length() == 1) minutes = "0" + minutes;

        return hours + ":" + minutes;
    }

    public static double getFatigueToReplenish(Player player) {
        long gameTime = player.level.getGameTime();
        long wakeTime = SomniaUtil.calculateWakeTime(player.level, player.level.isNight() ? 0 : 12000); // TODO
        return SomniaConfig.COMMON.fatigueReplenishRate.get() * (wakeTime - gameTime);
    }

    public static boolean isEnterSleepTime() {
        return 24000 >= SomniaConfig.COMMON.enterSleepStart.get() && 24000 <= SomniaConfig.COMMON.enterSleepEnd.get();
    }

    public static boolean isValidSleepTime(ServerLevel level) {
        long time = level.getGameTime() % 24000;
        return time >= SomniaConfig.COMMON.validSleepStart.get() && time <= SomniaConfig.COMMON.validSleepEnd.get();
    }

    private SomniaUtil() {}
}
