package mods.su5ed.somnia.core;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.compat.Coffees;
import mods.su5ed.somnia.config.ConfigHolder;
import mods.su5ed.somnia.handler.ClientTickHandler;
import mods.su5ed.somnia.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("somnia")
public class Somnia {
    public static final String MODID = "somnia";
    public static final Logger LOGGER = LogManager.getLogger();

    public Somnia() {
        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(ClientTickHandler.INSTANCE));
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityFatigue.register();
        NetworkHandler.registerMessages();
        Coffees.registerCoffees();
    }
}
