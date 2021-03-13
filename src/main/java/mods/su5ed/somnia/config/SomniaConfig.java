package mods.su5ed.somnia.config;

import java.util.List;

public class SomniaConfig {
    //Fatigue
    public static String displayFatigue;
    public static boolean simpleFatigueDisplay;
    public static String displayETASleep;
    public static double fatigueRate;
    public static double fatigueReplenishRate;
    public static boolean fatigueSideEffects;
    public static double minimumFatigueToSleep;
    public static List<? extends List<Integer>> sideEffectStages;

    //Logic
    public static double delta;
    public static double baseMultiplier;
    public static double multiplierCap;

    //Options
    public static boolean fading;
    public static boolean ignoreMonsters;
    public static boolean muteSoundWhenSleeping;
    public static boolean sleepWithArmor;
    public static boolean somniaGui;
    public static String wakeTimeSelectItem;

    //Performance
    public static boolean disableCreatureSpawning;
    public static boolean disableRendering;

    //Timings
    public static int enterSleepStart;
    public static int enterSleepEnd;

    public static int validSleepStart;
    public static int validSleepEnd;
}
