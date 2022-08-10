package dev.su5ed.somnia;

import dev.su5ed.somnia.capability.Fatigue;
import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.network.SomniaNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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

@Mod(SomniaAwoken.MODID)
public class SomniaAwoken {
    public static final String MODID = "somnia";
    public static final Logger LOGGER = LogManager.getLogger();

    public SomniaAwoken() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        
        MinecraftForge.EVENT_BUS.addListener(this::registerCapabilities);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(ClientSleepHandler.INSTANCE));

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
    
    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(Fatigue.class);
    }
    
    public void registerCommands(final RegisterCommandsEvent event) {
        SomniaCommand.register(event.getDispatcher());
    }
}
