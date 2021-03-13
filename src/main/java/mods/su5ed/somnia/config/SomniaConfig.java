package mods.su5ed.somnia.config;

import java.util.List;

public class SomniaConfig {
    //Fatigue
    public static String displayFatigue;
    public static String displayETASleep;
    public static double fatigueRate;
    public static double fatigueReplenishRate;
    public static boolean fatigueSideEffects;
    public static double minimumFatigueToSleep;
    public static List<? extends List<Integer>> sideEffectStages;

    //Side Effects
    public static int sideEffectStage1;
    public static int sideEffectStage1Potion;
    public static int sideEffectStage1Duration;
    public static int sideEffectStage1Amplifier;

    public static int sideEffectStage2;
    public static int sideEffectStage2Potion;
    public static int sideEffectStage2Duration;
    public static int sideEffectStage2Amplifier;

    public static int sideEffectStage3;
    public static int sideEffectStage3Potion;
    public static int sideEffectStage3Duration;
    public static int sideEffectStage3Amplifier;

    public static int sideEffectStage4;
    public static int sideEffectStage4Potion;
    public static int sideEffectStage4Amplifier;

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
