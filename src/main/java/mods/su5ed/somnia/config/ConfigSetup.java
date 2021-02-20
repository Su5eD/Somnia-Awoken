package mods.su5ed.somnia.config;

import mods.su5ed.somnia.core.Somnia;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

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
        SomniaConfig.displayETASleep = ConfigHolder.CLIENT.displayETASleep.get();

        SomniaConfig.somniaGui = ConfigHolder.CLIENT.somniaGui.get();

        SomniaConfig.disableRendering = ConfigHolder.CLIENT.disableRendering.get();
    }

    private static void updateCommonConfig() {
        SomniaConfig.fatigueRate = ConfigHolder.COMMON.fatigueRate.get();
        SomniaConfig.fatigueReplenishRate = ConfigHolder.COMMON.fatigueReplenishRate.get();
        SomniaConfig.fatigueSideEffects = ConfigHolder.COMMON.fatigueSideEffects.get();
        SomniaConfig.minimumFatigueToSleep = ConfigHolder.COMMON.minimumFatigueToSleep.get();
    
        SomniaConfig.sideEffectStage1 = ConfigHolder.COMMON.sideEffectStage1.get();
        SomniaConfig.sideEffectStage1Potion = ConfigHolder.COMMON.sideEffectStage1Potion.get();
        SomniaConfig.sideEffectStage1Duration = ConfigHolder.COMMON.sideEffectStage1Duration.get();
        SomniaConfig.sideEffectStage1Amplifier = ConfigHolder.COMMON.sideEffectStage1Amplifier.get();

        SomniaConfig.sideEffectStage2 = ConfigHolder.COMMON.sideEffectStage2.get();
        SomniaConfig.sideEffectStage2Potion = ConfigHolder.COMMON.sideEffectStage2Potion.get();
        SomniaConfig.sideEffectStage2Duration = ConfigHolder.COMMON.sideEffectStage2Duration.get();
        SomniaConfig.sideEffectStage2Amplifier = ConfigHolder.COMMON.sideEffectStage2Amplifier.get();

        SomniaConfig.sideEffectStage3 = ConfigHolder.COMMON.sideEffectStage3.get();
        SomniaConfig.sideEffectStage3Potion = ConfigHolder.COMMON.sideEffectStage3Potion.get();
        SomniaConfig.sideEffectStage3Duration = ConfigHolder.COMMON.sideEffectStage3Duration.get();
        SomniaConfig.sideEffectStage3Amplifier = ConfigHolder.COMMON.sideEffectStage3Amplifier.get();

        SomniaConfig.sideEffectStage4 = ConfigHolder.COMMON.sideEffectStage4.get();
        SomniaConfig.sideEffectStage4Potion = ConfigHolder.COMMON.sideEffectStage4Potion.get();
        SomniaConfig.sideEffectStage4Amplifier = ConfigHolder.COMMON.sideEffectStage4Amplifier.get();
    
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
}
