package mods.su5ed.somnia.handler;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.CapabilityFatigueProvider;
import mods.su5ed.somnia.core.SomniaCommand;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketUpdateFatigue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RegistryHandler {
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
        sync((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync((ServerPlayerEntity) event.getPlayer());
    }

    private static void sync(ServerPlayerEntity player) {
        player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> NetworkHandler.sendToClient(new PacketUpdateFatigue(props.getFatigue()), player));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getEntity().world.isRemote) {
            event.getOriginal().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> {
                CompoundNBT old = props.serializeNBT();
                event.getPlayer().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(fatigue -> {
                    fatigue.deserializeNBT(old);
                });
            });
        }
    }
}
