package mods.su5ed.somnia.util;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.config.SomniaConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SomniaUtil {
    public static boolean doesPlayerWearArmor(PlayerEntity player) {
        return player.inventory.armor.stream()
                .anyMatch(stack -> !stack.isEmpty());
    }

    public static int getLevelDayTime(World world) {
        return (int) (world.getDayTime() % 24000L);
    }

    public static long calculateWakeTime(World world, int target) {
        return calculateWakeTime(world.getGameTime(), getLevelDayTime(world), target);
    }

    public static long calculateWakeTime(long gameTime, long dayTime, int target) {
        long wakeTime = gameTime - dayTime + target;
        return dayTime > target ? wakeTime + 24000L : wakeTime;
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
        long wakeTime = SomniaUtil.calculateWakeTime(worldTime, player.level.getDayTime(), player.level.isNight() ? 0 : 12000);
        return SomniaConfig.fatigueReplenishRate * (wakeTime - worldTime);
    }

    public static boolean isEnterSleepTime(World world) {
        long time = getLevelDayTime(world);
        return time >= SomniaConfig.enterSleepStart && time <= SomniaConfig.enterSleepEnd;
    }

    public static boolean isValidSleepTime(ServerWorld world) {
        long time = getLevelDayTime(world);
        return time >= SomniaConfig.validSleepStart && time <= SomniaConfig.validSleepEnd;
    }
}
