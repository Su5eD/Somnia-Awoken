package mods.su5ed.somnia.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHolder {
    
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    protected static CommonConfig COMMON;
    protected static ClientConfig CLIENT;

    static {
        Pair<CommonConfig, ForgeConfigSpec> CommonSpecPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = CommonSpecPair.getLeft();
        COMMON_SPEC = CommonSpecPair.getRight();

        Pair<ClientConfig, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
    }

    protected static final class ClientConfig {
        protected final ForgeConfigSpec.ConfigValue<String> displayFatigue;
        protected final ForgeConfigSpec.ConfigValue<String> displayETASleep;
        protected final ForgeConfigSpec.BooleanValue somniaGui;
        protected final ForgeConfigSpec.BooleanValue disableRendering;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("fatigue");
            displayFatigue = builder
                    .comment("The fatigue counter's position. Accepted values: TOM_CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT")
                    .define("displayFatigue", "BOTTOM_RIGHT");
            displayETASleep = builder
                    .comment("The ETA and multiplier display position in Somnia's sleep gui. Accepted values: right, center, left")
                    .define("displayETASleep", "left");
            builder.pop();

            builder.push("options");
            somniaGui = builder
                    .comment("Provides an enhanced sleeping gui")
                    .define("somniaGui", true);
            builder.pop();

            builder.push("performance");
            disableRendering = builder
                    .comment("Disable rendering while you're asleep")
                    .define("disableRendering", false);
            builder.pop();
        }
    }

    protected static final class CommonConfig {
        protected final ForgeConfigSpec.ConfigValue<Double> fatigueRate;
        protected final ForgeConfigSpec.ConfigValue<Double> fatigueReplenishRate;
        protected final ForgeConfigSpec.BooleanValue fatigueSideEffects;
        protected final ForgeConfigSpec.ConfigValue<Integer> minimumFatigueToSleep;

        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage1;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage1Potion;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage1Duration;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage1Amplifier;

        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage2;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage2Potion;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage2Duration;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage2Amplifier;

        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage3;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage3Potion;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage3Duration;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage3Amplifier;

        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage4;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage4Potion;
        protected final ForgeConfigSpec.ConfigValue<Integer> sideEffectStage4Amplifier;

        protected final ForgeConfigSpec.ConfigValue<Double> delta;
        protected final ForgeConfigSpec.ConfigValue<Double> baseMultiplier;
        protected final ForgeConfigSpec.ConfigValue<Double> multiplierCap;

        protected final ForgeConfigSpec.BooleanValue fading;
        protected final ForgeConfigSpec.BooleanValue ignoreMonsters;
        protected final ForgeConfigSpec.BooleanValue muteSoundWhenSleeping;
        protected final ForgeConfigSpec.BooleanValue sleepWithArmor;
        protected final ForgeConfigSpec.ConfigValue<String> wakeTimeSelectItem;

        protected final ForgeConfigSpec.BooleanValue disableCreatureSpawning;
        protected final ForgeConfigSpec.BooleanValue disableMoodSoundAndLightCheck;

        protected final ForgeConfigSpec.ConfigValue<Integer> enterSleepStart;
        protected final ForgeConfigSpec.ConfigValue<Integer> enterSleepEnd;

        protected final ForgeConfigSpec.ConfigValue<Integer> validSleepStart;
        protected final ForgeConfigSpec.ConfigValue<Integer> validSleepEnd;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("fatigue");
            fatigueRate = builder
                    .comment("Fatigue is incremented by this number every tick")
                    .define("fatigueRate", 0.00208);
            fatigueReplenishRate = builder
                    .comment("Fatigue is decreased by this number while you sleep (every tick)")
                    .define("fatigueReplenishRate", 0.00833);
            fatigueSideEffects = builder
                    .comment("Enables fatigue side effects")
                    .define("fatigueSideEffects", true);
            minimumFatigueToSleep = builder
                    .comment("The required amount of fatigue to sleep")
                    .define("minimumFatigueToSleep", 20);
            builder.pop();

            builder.comment("Fatigue levels to enter each side effect stage, their potion IDs, amplifiers and duration (ticks)").push("fatigueSideEffects");
            sideEffectStage1 = builder
                    .comment("Amount of fatigue required to enter the first side effect stage")
                    .define("sideEffectStage1", 70);
            sideEffectStage1Potion = builder
                    .comment("Potion effect ID of the first stage")
                    .define("sideEffectStage1Potion", 9);
            sideEffectStage1Duration = builder
                    .comment("Effect duration of the first stage")
                    .define("sideEffectStage1Duration", 150);
            sideEffectStage1Amplifier = builder
                    .comment("Potion effect amplifier of the first stage")
                    .define("sideEffectStage1Amplifier", 0);

            sideEffectStage2 = builder
                    .comment("Amount of fatigue required to enter the second side effect stage")
                    .define("sideEffectStage2", 80);
            sideEffectStage2Potion = builder
                    .comment("Potion effect ID of the second stage")
                    .define("sideEffectStage2Potion", 2);
            sideEffectStage2Duration = builder
                    .comment("Effect duration of the second stage")
                    .define("sideEffectStage2Duration", 300);
            sideEffectStage2Amplifier = builder
                    .comment("Potion effect amplifier of the second stage")
                    .define("sideEffectStage2Amplifier", 2);

            sideEffectStage3 = builder
                    .comment("Amount of fatigue required to enter the third side effect stage")
                    .define("sideEffectStage3", 90);
            sideEffectStage3Potion = builder
                    .comment("Potion effect ID of the third stage")
                    .define("sideEffectStage3Potion", 19);
            sideEffectStage3Duration = builder
                    .comment("Effect duration of the third stage")
                    .define("sideEffectStage3Duration", 200);
            sideEffectStage3Amplifier = builder
                    .comment("Potion effect amplifier of the third stage")
                    .define("sideEffectStage3Amplifier", 1);

            sideEffectStage4 = builder
                    .comment("Amount of fatigue required to enter the fourth side effect stage")
                    .define("sideEffectStage4", 95);
            sideEffectStage4Potion = builder
                    .comment("Potion effect ID of the fourth stage")
                    .define("sideEffectStage4Potion", 2);
            sideEffectStage4Amplifier = builder
                    .comment("Potion effect amplifier of the fourth stage")
                    .define("sideEffectStage4Amplifier", 3);
            builder.pop();

            builder.push("logic");
            delta = builder
                    .comment("If the time difference (mc) between multiplied ticking is greater than this, the simulation multiplier is lowered. Otherwise, it's increased. Lowering this number might slow down simulation and improve performance. Don't mess around with it if you don't know what you're doing.")
                    .defineInRange("delta", 50D, 1D, 50D);
            baseMultiplier = builder
                    .comment("Minimum tick speed multiplier, activated during sleep")
                    .define("baseMultiplier", 1D);
            multiplierCap = builder
                    .comment("Maximum tick speed multiplier, activated during sleep")
                    .define("multiplierCap", 100D);
            builder.pop();

            builder.push("options");
            fading = builder
                    .comment("Slightly slower sleep start/end")
                    .define("fading", true);
            ignoreMonsters = builder
                    .comment("Let the player sleep even when there are monsters nearby")
                    .define("ignoreMonsters", false);
            muteSoundWhenSleeping = builder
                    .comment("Deafens you while you're asleep. Mob sounds are confusing with the world sped up")
                    .define("muteSoundWhenSleeping", false);
            sleepWithArmor = builder
                    .comment("Allows you to sleep with armor equipped")
                    .define("sleepWithArmor", false);
            wakeTimeSelectItem = builder
                    .comment("the item used to select wake time")
                    .define("wakeTimeSelectItem","minecraft:clock");
            builder.pop();

            builder.push("performance");
            disableCreatureSpawning = builder
                    .comment("Disables mob spawning while you sleep")
                    .define("disableCreatureSpawning", false);
            disableMoodSoundAndLightCheck = builder
                    .comment("Disabled chunk light checking from being called every tick while you sleep")
                    .define("disableMoodSoundAndLightCheck", false);
            builder.pop();

            builder.push("timings");
            enterSleepStart = builder
                    .comment("Specifies the start of the period in which the player can enter sleep")
                    .defineInRange("enterSleepStart", 0, 0, 24000);
            enterSleepEnd = builder
                    .comment("Specifies the end of the period in which the player can enter sleep")
                    .defineInRange("enterSleepEnd", 24000, 0, 24000);

            validSleepStart = builder
                    .comment("Specifies the start of the valid sleep period")
                    .defineInRange("validSleepStart", 0, 0, 24000);
            validSleepEnd = builder
                    .comment("Specifies the end of the valid sleep period")
                    .defineInRange("validSleepEnd", 24000, 0, 24000);
            builder.pop();
        }
    }
}
