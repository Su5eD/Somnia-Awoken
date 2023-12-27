package dev.su5ed.somnia;

import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.network.SomniaNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SomniaAwoken.MODID)
public class SomniaAwoken {
    public static final String MODID = "somnia";
    public static final Logger LOGGER = LogManager.getLogger();

    public SomniaAwoken(IEventBus bus, Dist dist) {
        bus.addListener(this::setup);

        bus.addListener(SomniaObjects::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(SomniaObjects::registerCommands);
        if (dist.isClient()) {
            bus.addListener(ClientSetup::registerGuiOverlays);
            NeoForge.EVENT_BUS.register(ClientSleepHandler.INSTANCE);
        }

        SomniaObjects.register(bus);
        SomniaNetwork.registerMessages();

        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, SomniaConfig.COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, SomniaConfig.CLIENT_SPEC);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModList modList = ModList.get();
        Compat.comfortsLoaded = modList.isLoaded(Compat.COMFORTS_MODID);
        Compat.curiosLoaded = modList.isLoaded(Compat.CURIOS_MODID);
        Compat.darkUtilsLoaded = modList.isLoaded(Compat.DARK_UTILS_MODID);

        event.enqueueWork(SomniaObjects::registerBrewingRecipes);
    }
}
