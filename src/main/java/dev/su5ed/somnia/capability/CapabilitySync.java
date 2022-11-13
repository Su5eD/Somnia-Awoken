package dev.su5ed.somnia.capability;

import dev.su5ed.somnia.SomniaAwoken;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.FatigueUpdatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SomniaAwoken.MODID)
public final class CapabilitySync {

    @SubscribeEvent
    public static void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(CapabilityFatigue.NAME, new CapabilityFatigueProvider());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getEntity().level.isClientSide && event.isWasDeath()) {
            event.getOriginal().getCapability(CapabilityFatigue.INSTANCE)
                .ifPresent(props -> {
                    CompoundTag old = props.serializeNBT();
                    event.getEntity().getCapability(CapabilityFatigue.INSTANCE)
                        .ifPresent(fatigue -> fatigue.deserializeNBT(old));
                });
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        event.getEntity().getCapability(CapabilityFatigue.INSTANCE)
            .ifPresent(props -> {
                props.setFatigue(0);
                props.setReplenishedFatigue(0);
                props.setExtraFatigueRate(0);
            });
    }

    private static void sync(ServerPlayer player) {
        player.getCapability(CapabilityFatigue.INSTANCE)
            .ifPresent(props -> SomniaNetwork.sendToClient(new FatigueUpdatePacket(props.getFatigue()), player));
    }

    private CapabilitySync() {}
}
