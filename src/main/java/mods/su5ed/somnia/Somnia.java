package mods.su5ed.somnia;

import mods.su5ed.somnia.api.capability.FatigueCapability;
import mods.su5ed.somnia.common.ForgeEventHandler;
import mods.su5ed.somnia.common.PlayerSleepTickHandler;
import mods.su5ed.somnia.config.ConfigHolder;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.server.ServerTickHandler;
import mods.su5ed.somnia.util.TimePeriod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Mod("somnia")
public class Somnia {
    public static final String MODID = "somnia";
    public static final Logger LOGGER = LogManager.getLogger();
    public static Somnia instance;

    public final List<ServerTickHandler> tickHandlers = new ArrayList<>();
    public final List<WeakReference<ServerPlayerEntity>> ignoreList = new ArrayList<>();
    public static ForgeEventHandler forgeEventHandler;
    public static TimePeriod enterSleepPeriod;
    public static TimePeriod validSleepPeriod;

    public Somnia() {
        instance = this;
        forgeEventHandler = new ForgeEventHandler();

        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(forgeEventHandler);
        MinecraftForge.EVENT_BUS.register(new PlayerSleepTickHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        FatigueCapability.register();
        NetworkHandler.registerMessages();

        enterSleepPeriod = new TimePeriod(SomniaConfig.enterSleepStart, SomniaConfig.enterSleepEnd);
        validSleepPeriod = new TimePeriod(SomniaConfig.validSleepStart, SomniaConfig.validSleepEnd);
    }
}
