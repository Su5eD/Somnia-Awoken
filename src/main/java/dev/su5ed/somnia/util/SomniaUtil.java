package dev.su5ed.somnia.util;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.core.SomniaConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class SomniaUtil {
    public static boolean doesPlayerWearArmor(Player player) {
        return player.getInventory().armor.stream()
                .anyMatch(stack -> !stack.isEmpty());
    }

    public static long calculateWakeTime(long totalWorldTime, int target) {
        long timeInDay = totalWorldTime % 24000;
        long wakeTime = totalWorldTime - timeInDay + target;
        return timeInDay > target ? wakeTime + 24000 : wakeTime;
    }

    public static boolean checkFatigue(Player player) {
        return player.getCapability(CapabilityFatigue.INSTANCE)
                .map(props -> player.isCreative() || props.getFatigue() >= SomniaConfig.COMMON.minimumFatigueToSleep.get())
                .orElse(false);
    }

    public static String timeStringForWorldTime(long time) {
        time += 6000;

        time = time % 24000;
        String hours = String.valueOf(time / 1000);
        String minutes = String.valueOf((int) ((time % 1000) / 1000D * 60));

        if (hours.length() == 1) hours = "0" + hours;
        if (minutes.length() == 1) minutes = "0" + minutes;

        return hours + ":" + minutes;
    }

    public static double getFatigueToReplenish(Player player) {
        long worldTime = player.level.getGameTime();
        long wakeTime = SomniaUtil.calculateWakeTime(worldTime, player.level.isNight() ? 0 : 12000);
        return SomniaConfig.COMMON.fatigueReplenishRate.get() * (wakeTime - worldTime);
    }

    public static boolean isEnterSleepTime() {
        return 24000 >= SomniaConfig.COMMON.enterSleepStart.get() && 24000 <= SomniaConfig.COMMON.enterSleepEnd.get();
    }

    public static boolean isValidSleepTime(ServerLevel world) {
        long time = world.getGameTime() % 24000;
        return time >= SomniaConfig.COMMON.validSleepStart.get() && time <= SomniaConfig.COMMON.validSleepEnd.get();
    }
    
    private SomniaUtil() {}
}
