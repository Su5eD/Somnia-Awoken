package mods.su5ed.somnia.config;

import mods.su5ed.somnia.core.Somnia;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Somnia.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@SuppressWarnings("unused")
public class ConfigSetup {

    @SubscribeEvent
    public static void onModConfig(ModConfig.ModConfigEvent event) {
        ForgeConfigSpec spec = event.getConfig().getSpec();
        if (spec == ConfigHolder.COMMON_SPEC) updateCommonConfig();
        else if (spec == ConfigHolder.CLIENT_SPEC) updateClientConfig();
    }

    private static void updateClientConfig() {
        SomniaConfig.displayFatigue = ConfigHolder.CLIENT.displayFatigue.get();
        SomniaConfig.simpleFatigueDisplay = ConfigHolder.CLIENT.simpleFatigueDisplay.get();
        SomniaConfig.displayETASleep = ConfigHolder.CLIENT.displayETASleep.get();

        SomniaConfig.somniaGui = ConfigHolder.CLIENT.somniaGui.get();
        SomniaConfig.somniaGuiClockPosition = ConfigHolder.CLIENT.somniaGuiClockPosition.get();

        SomniaConfig.disableRendering = ConfigHolder.CLIENT.disableRendering.get();
    }

    private static void updateCommonConfig() {
        SomniaConfig.fatigueRate = ConfigHolder.COMMON.fatigueRate.get();
        SomniaConfig.fatigueReplenishRate = ConfigHolder.COMMON.fatigueReplenishRate.get();
        SomniaConfig.fatigueSideEffects = ConfigHolder.COMMON.fatigueSideEffects.get();
        SomniaConfig.minimumFatigueToSleep = ConfigHolder.COMMON.minimumFatigueToSleep.get();
        SomniaConfig.sideEffectStages = ConfigHolder.COMMON.sideEffectStages.get();
        SomniaConfig.replenishingItems = ConfigHolder.COMMON.replenishingItems.get()
                .stream()
                .map(list -> Pair.of(getModItem((String) list.get(0)), Double.parseDouble(list.get(1).toString())))
                .filter(pair -> pair.getLeft() != null)
                .collect(Collectors.toList());

        SomniaConfig.delta = ConfigHolder.COMMON.delta.get();
        SomniaConfig.baseMultiplier = ConfigHolder.COMMON.baseMultiplier.get();
        SomniaConfig.multiplierCap = ConfigHolder.COMMON.multiplierCap.get();
    
        SomniaConfig.fading = ConfigHolder.COMMON.fading.get();
        SomniaConfig.ignoreMonsters = ConfigHolder.COMMON.ignoreMonsters.get();
        SomniaConfig.muteSoundWhenSleeping = ConfigHolder.COMMON.muteSoundWhenSleeping.get();
        SomniaConfig.sleepWithArmor = ConfigHolder.COMMON.sleepWithArmor.get();
        SomniaConfig.wakeTimeSelectItem = ConfigHolder.COMMON.wakeTimeSelectItem.get();
    
        SomniaConfig.disableCreatureSpawning = ConfigHolder.COMMON.disableCreatureSpawning.get();
    
        SomniaConfig.enterSleepStart = ConfigHolder.COMMON.enterSleepStart.get();
        SomniaConfig.enterSleepEnd = ConfigHolder.COMMON.enterSleepEnd.get();

        SomniaConfig.validSleepStart = ConfigHolder.COMMON.validSleepStart.get();
        SomniaConfig.validSleepEnd = ConfigHolder.COMMON.validSleepEnd.get();
    }

    private static Item getModItem(String registryName) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
    }
}
