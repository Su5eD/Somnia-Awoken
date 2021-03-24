package mods.su5ed.somnia.util;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.config.SomniaConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class SomniaUtil {
    public static boolean doesPlayerWearArmor(PlayerEntity player) {
        return player.inventory.armor.stream()
                .anyMatch(stack -> !stack.isEmpty());
    }

    public static long calculateWakeTime(long totalWorldTime, int target) {
        long timeInDay = totalWorldTime % 24000;
        long wakeTime = totalWorldTime - timeInDay + target;
        return timeInDay > target ? wakeTime + 24000 : wakeTime;
    }

    public static boolean checkFatigue(PlayerEntity player) {
        return player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
                .map(props -> player.isCreative() || props.getFatigue() >= SomniaConfig.minimumFatigueToSleep)
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

    public static double getFatigueToReplenish(PlayerEntity player) {
        long worldTime = player.level.getGameTime();
        long wakeTime = SomniaUtil.calculateWakeTime(worldTime, player.level.isNight() ? 0 : 12000);
        return SomniaConfig.fatigueReplenishRate * (wakeTime - worldTime);
    }

    public static boolean isEnterSleepTime() {
        return 24000 >= SomniaConfig.enterSleepStart && 24000 <= SomniaConfig.enterSleepEnd;
    }

    public static boolean isValidSleepTime(ServerWorld world) {
        long time = world.getGameTime() % 24000;
        return time >= SomniaConfig.validSleepStart && time <= SomniaConfig.validSleepEnd;
    }
}
