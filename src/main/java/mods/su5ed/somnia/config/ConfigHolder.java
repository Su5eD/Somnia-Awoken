package mods.su5ed.somnia.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

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
        protected final ForgeConfigSpec.BooleanValue simpleFatigueDisplay;
        protected final ForgeConfigSpec.ConfigValue<String> displayETASleep;
        protected final ForgeConfigSpec.BooleanValue somniaGui;
        protected final ForgeConfigSpec.BooleanValue disableRendering;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("fatigue");
            displayFatigue = builder
                    .comment("The fatigue counter's position. Accepted values: TOP_CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT")
                    .define("displayFatigue", "BOTTOM_RIGHT");
            simpleFatigueDisplay = builder
                    .comment("Simplifies the numerical fatigue counter to words")
                    .define("simpleFatigueDisplay", false);
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
        protected final ForgeConfigSpec.ConfigValue<List<? extends List<Integer>>> sideEffectStages;

        protected final ForgeConfigSpec.ConfigValue<Double> delta;
        protected final ForgeConfigSpec.ConfigValue<Double> baseMultiplier;
        protected final ForgeConfigSpec.ConfigValue<Double> multiplierCap;

        protected final ForgeConfigSpec.BooleanValue fading;
        protected final ForgeConfigSpec.BooleanValue ignoreMonsters;
        protected final ForgeConfigSpec.BooleanValue muteSoundWhenSleeping;
        protected final ForgeConfigSpec.BooleanValue sleepWithArmor;
        protected final ForgeConfigSpec.ConfigValue<String> wakeTimeSelectItem;

        protected final ForgeConfigSpec.BooleanValue disableCreatureSpawning;

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
            sideEffectStages = builder
                    .comment("Definitions of each side effect stage in order: min fatigue, max fatigue, potion ID, duration, amplifier. For a permanent effect, set the duration to -1.")
                    .defineList("sideEffectStages", Arrays.asList(
                            Arrays.asList(70, 80, 9, 150, 0),
                            Arrays.asList(80, 90, 2, 300, 2),
                            Arrays.asList(90, 95, 19, 200, 1),
                            Arrays.asList(95, 100, 2, -1, 3)
                    ), obj -> obj instanceof List);
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
