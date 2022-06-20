package dev.su5ed.somnia.core;

import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.config.ConfigHolder;
import dev.su5ed.somnia.handler.ClientTickHandler;
import dev.su5ed.somnia.network.NetworkHandler;
import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Somnia.MODID)
public class Somnia {
    public static final String MODID = "somnia";
    public static final Logger LOGGER = LogManager.getLogger();

    public Somnia() {
        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(ClientTickHandler.INSTANCE));

        SomniaObjects.EFFECTS.register(bus);
        SomniaObjects.POTIONS.register(bus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityFatigue.register();
        NetworkHandler.registerMessages();

        Compat.comforts = ModList.get().isLoaded("comforts");

        PotionBrewing.addMix(Potions.NIGHT_VISION, Items.GLISTERING_MELON_SLICE, SomniaObjects.AWAKENING_POTION.get());
        PotionBrewing.addMix(Potions.LONG_NIGHT_VISION, Items.GLISTERING_MELON_SLICE, SomniaObjects.LONG_AWAKENING_POTION.get());
        PotionBrewing.addMix(Potions.NIGHT_VISION, Items.BLAZE_POWDER, SomniaObjects.STRONG_AWAKENING_POTION.get());

        PotionBrewing.addMix(SomniaObjects.AWAKENING_POTION.get(), Items.FERMENTED_SPIDER_EYE, SomniaObjects.INSOMNIA_POTION.get());
        PotionBrewing.addMix(SomniaObjects.LONG_AWAKENING_POTION.get(), Items.FERMENTED_SPIDER_EYE, SomniaObjects.LONG_INSOMNIA_POTION.get());
        PotionBrewing.addMix(SomniaObjects.STRONG_AWAKENING_POTION.get(), Items.FERMENTED_SPIDER_EYE, SomniaObjects.STRONG_INSOMNIA_POTION.get());
    }
}
