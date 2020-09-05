package mods.su5ed.somnia;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.common.PlayerSleepTickHandler;
import mods.su5ed.somnia.common.config.ConfigHolder;
import mods.su5ed.somnia.common.config.SomniaConfig;
import mods.su5ed.somnia.common.util.SomniaState;
import mods.su5ed.somnia.network.PacketHandler;
import mods.su5ed.somnia.server.ForgeEventHandler;
import mods.su5ed.somnia.server.ServerTickHandler;
import mods.su5ed.somnia.server.SomniaCommand;
import mods.su5ed.somnia.setup.ClientProxy;
import mods.su5ed.somnia.setup.IProxy;
import mods.su5ed.somnia.setup.ServerProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Mod("somnia")
public class Somnia
{
    public static final String MOD_ID = "somnia";
    public static final String NAME = "Somnia";
    public static final String VERSION = SomniaVersion.getVersionString();

    public static Somnia instance;
    public static IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public final List<ServerTickHandler> tickHandlers;
    public List<WeakReference<ServerPlayerEntity>> ignoreList;
    public static ForgeEventHandler forgeEventHandler;

    public static final Logger LOGGER = LogManager.getLogger();

    public static EventNetworkChannel eventChannel;

    public static long clientAutoWakeTime = -1;

    public Somnia() {
        instance = this;
        this.tickHandlers = new ArrayList<>();
        this.ignoreList = new ArrayList<>();
        forgeEventHandler = new ForgeEventHandler();

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(forgeEventHandler);
        MinecraftForge.EVENT_BUS.register(new PlayerSleepTickHandler());
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        eventChannel = NetworkRegistry.newEventChannel(PacketHandler.CHANNEL_ID, () -> "1.0.0", s -> true, s -> true);
        eventChannel.registerObject(new PacketHandler());

        proxy.register();
        CapabilityFatigue.register();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onCommandRegister(RegisterCommandsEvent event) {
        SomniaCommand.register(event.getDispatcher());
    }

    public static String timeStringForWorldTime(long time)
    {
        time += 6000; // Tick -> Time offset

        time = time % 24000;
        int hours = (int) Math.floor(time / (double)1000);
        int minutes = (int) ((time % 1000) / 1000.0d * 60);

        String lsHours = String.valueOf(hours);
        String lsMinutes = String.valueOf(minutes);

        if (lsHours.length() == 1)
            lsHours = "0"+lsHours;
        if (lsMinutes.length() == 1)
            lsMinutes = "0"+lsMinutes;

        return lsHours + ":" + lsMinutes;
    }

    @SuppressWarnings("unused")
    public static void tick()
    {
        synchronized (Somnia.instance.tickHandlers)
        {
            for (ServerTickHandler serverTickHandler : Somnia.instance.tickHandlers)
                serverTickHandler.tickStart();
        }
    }

    @SuppressWarnings("unused")
    public static boolean doesPlayHaveAnyArmor(PlayerEntity e)
    {
        ItemStack[] armor = e.inventory.armorInventory.toArray(new ItemStack[0]);
        for (ItemStack itemStack : armor) {
            if (itemStack != ItemStack.EMPTY)
                return true;
        }
        return false;
    }

    public static long calculateWakeTime(long totalWorldTime, int i)
    {
        long l;
        long timeInDay = totalWorldTime % 24000L;
        l = totalWorldTime - timeInDay + i;
        if (timeInDay > i)
            l += 24000L;
        return l;
    }

    @SuppressWarnings("unused")
    public static boolean doMobSpawning(ServerWorld par1WorldServer)
    {
        boolean defValue = par1WorldServer.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
        if (!SomniaConfig.disableCreatureSpawning || !defValue)
            return defValue;

        for (ServerTickHandler serverTickHandler : instance.tickHandlers)
        {
            if (serverTickHandler.worldServer == par1WorldServer)
                return serverTickHandler.currentState != SomniaState.ACTIVE;
        }

        throw new IllegalStateException("tickHandlers doesn't contain match for given world server");
    }
}
