package dev.su5ed.somnia.handler;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.api.capability.CapabilityFatigueProvider;
import dev.su5ed.somnia.api.capability.IFatigue;
import dev.su5ed.somnia.core.Somnia;
import dev.su5ed.somnia.core.SomniaCommand;
import dev.su5ed.somnia.network.FatigueUpdatePacket;
import dev.su5ed.somnia.network.SomniaNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Somnia.MODID)
public final class RegistryHandler {

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IFatigue.class);
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        SomniaCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(CapabilityFatigue.NAME, new CapabilityFatigueProvider());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        sync((ServerPlayer) event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync((ServerPlayer) event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync((ServerPlayer) event.getPlayer());
    }

    private static void sync(ServerPlayer player) {
        player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> SomniaNetwork.sendToClient(new FatigueUpdatePacket(props.getFatigue()), player));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getEntity().level.isClientSide) {
            event.getOriginal().getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> {
                CompoundTag old = props.serializeNBT();
                event.getPlayer().getCapability(CapabilityFatigue.INSTANCE).ifPresent(fatigue -> fatigue.deserializeNBT(old));
            });
        }
    }

    private RegistryHandler() {}
}
