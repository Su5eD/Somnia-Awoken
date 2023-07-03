package dev.su5ed.somnia;

import dev.su5ed.somnia.api.ReplenishingItem;
import dev.su5ed.somnia.util.FatigueDisplayPosition;
import dev.su5ed.somnia.util.ScreenPosition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public final class SomniaConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final CommonConfig COMMON;
    public static final ClientConfig CLIENT;

    static {
        Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    private SomniaConfig() {}

    public static final class ClientConfig {
        public final ForgeConfigSpec.EnumValue<FatigueDisplayPosition> fatigueDisplayPos;
        public final ForgeConfigSpec.BooleanValue simpleFatigueDisplay;
        public final ForgeConfigSpec.EnumValue<ScreenPosition> displayETASleep;
        public final ForgeConfigSpec.BooleanValue somniaGui;
        public final ForgeConfigSpec.EnumValue<ScreenPosition> somniaGuiClockPosition;
        public final ForgeConfigSpec.BooleanValue disableRendering;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("fatigue");
            fatigueDisplayPos = builder
                .comment("The fatigue counter's position")
                .defineEnum("fatigueDisplayPos", FatigueDisplayPosition.BOTTOM_RIGHT);
            simpleFatigueDisplay = builder
                .comment("Simplifies the numerical fatigue counter to words")
                .define("simpleFatigueDisplay", false);
            displayETASleep = builder
                .comment("The ETA and multiplier display position in Somnia's sleep gui.")
                .defineEnum("displayETASleep", ScreenPosition.LEFT);
            builder.pop();

            builder.push("options");
            somniaGui = builder
                .comment("Provides an enhanced sleeping gui")
                .define("somniaGui", true);
            somniaGuiClockPosition = builder
                .comment("The display position of the clock in somnia's enhanced sleeping gui.")
                .defineEnum("somniaGuiClockPosition", ScreenPosition.RIGHT);
            builder.pop();

            builder.push("performance");
            disableRendering = builder
                .comment("Disable rendering while you're asleep")
                .define("disableRendering", false);
            builder.pop();
        }
    }

    public static final class CommonConfig {
        public final ForgeConfigSpec.BooleanValue enableFatigue;
        public final ForgeConfigSpec.DoubleValue fatigueRate;
        public final ForgeConfigSpec.DoubleValue fatigueReplenishRate;
        public final ForgeConfigSpec.BooleanValue fatigueSideEffects;
        public final ForgeConfigSpec.ConfigValue<Integer> minimumFatigueToSleep;
        public final ForgeConfigSpec.ConfigValue<List<? extends List<Object>>> sideEffectStages;
        public final ForgeConfigSpec.ConfigValue<List<? extends List<Object>>> replenishingItems;

        public final ForgeConfigSpec.DoubleValue delta;
        public final ForgeConfigSpec.DoubleValue minMultiplier;
        public final ForgeConfigSpec.DoubleValue maxMultiplier;

        public final ForgeConfigSpec.BooleanValue fading;
        public final ForgeConfigSpec.BooleanValue ignoreMonsters;
        public final ForgeConfigSpec.BooleanValue muteSoundWhenSleeping;
        public final ForgeConfigSpec.BooleanValue sleepWithArmor;
        public final ForgeConfigSpec.ConfigValue<String> wakeTimeSelectItem;
        public final ForgeConfigSpec.BooleanValue forceWakeUp;

        public final ForgeConfigSpec.BooleanValue disableCreatureSpawning;

        public final ForgeConfigSpec.IntValue enterSleepStart;
        public final ForgeConfigSpec.IntValue enterSleepEnd;

        public final ForgeConfigSpec.IntValue validSleepStart;
        public final ForgeConfigSpec.IntValue validSleepEnd;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("fatigue");
            enableFatigue = builder
                .comment("Master fatigue system override. Setting this to false will disable all fatigue-related logic as well as all config options in this category.")
                .define("enableFatigue", true);
            fatigueRate = builder
                .comment("Fatigue is incremented by this number every tick")
                .defineInRange("fatigueRate", 0.00208, 0.0, 1.0);
            fatigueReplenishRate = builder
                .comment("Fatigue is decreased by this number every tick while you sleep")
                .defineInRange("fatigueReplenishRate", 0.00833, 0.0, 1.0);
            fatigueSideEffects = builder
                .comment("Enables fatigue side effects")
                .define("fatigueSideEffects", true);
            minimumFatigueToSleep = builder
                .comment("The required amount of fatigue to sleep")
                .define("minimumFatigueToSleep", 20);
            sideEffectStages = builder
                .comment("Definitions of each side effect stage in order: min fatigue (int), max fatigue (int), effect name (resource location), duration (int), amplifier (int). For a permanent effect, set the duration to -1.")
                .defineList("sideEffectStages", List.of(
                    List.of(70, 80, "minecraft:nausea", 150, 0),
                    List.of(80, 90, "minecraft:slowness", 300, 2),
                    List.of(90, 95, "minecraft:poison", 200, 1),
                    List.of(95, 100, "minecraft:nausea", -1, 3)
                ), obj -> obj instanceof List);
            replenishingItems = builder
                .comment("Definitions of fatigue replenishing items. Each list consist of an item registry name, the amount of fatigue it replenishes, and optionally a fatigue rate modifier")
                .defineList("replenishingItems", List.of(
                    List.of("coffeespawner:coffee", 10),
                    List.of("coffeespawner:coffee_milk", 10),
                    List.of("coffeespawner:coffee_sugar", 15),
                    List.of("coffeespawner:coffee_milk_sugar", 15)
                ), obj -> obj instanceof List);
            builder.pop();

            builder.push("logic");
            delta = builder
                .comment("If the time difference (mc) between multiplied ticking is greater than this, the simulation multiplier is lowered. Otherwise, it's increased. Lowering this number might slow down simulation and improve performance. Don't mess around with it if you don't know what you're doing.")
                .defineInRange("delta", 50.0, 1.0, 50.0);
            minMultiplier = builder
                .worldRestart()
                .comment("Minimum tick speed multiplier, activated during sleep")
                .defineInRange("minMultiplier", 1.0, 1.0, 100.0);
            maxMultiplier = builder
                .worldRestart()
                .comment("Maximum tick speed multiplier, activated during sleep")
                .defineInRange("maxMultiplier", 100.0, 1.0, 100.0);
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
                .comment("The item used to select wake time. Use an empty string to disable, or an asterisk ('*') to enable at all times.")
                .define("wakeTimeSelectItem", "minecraft:clock");
            forceWakeUp = builder
                .comment("Force the player to wake up when fatigue reaches 0")
                .define("forceWakeUp", false);
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

        public List<ReplenishingItem> getReplenishingItems() {
            return replenishingItems.get().stream()
                .map(list -> {
                    Item item = getModItem((String) list.get(0));
                    double replenishedFatigue = Double.parseDouble(list.get(1).toString());
                    double fatigueRateModifier = list.size() > 2 ? Double.parseDouble(list.get(2).toString()) : fatigueRate.get();
                    return new ReplenishingItem(item, replenishedFatigue, fatigueRateModifier);
                })
                .filter(replenishingItem -> replenishingItem.item() != null)
                .toList();
        }

        public boolean isWakeTimeSelectionItem(ItemStack stack) {
            String matcher = wakeTimeSelectItem.get();
            return !matcher.isEmpty() && (matcher.equals("*") || !stack.isEmpty() && ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().equals(matcher));
        }

        private static Item getModItem(String registryName) {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        }
    }
}
