package dev.su5ed.somnia.capability;

import dev.su5ed.somnia.SomniaAwoken;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.client.FatigueUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod.EventBusSubscriber(modid = SomniaAwoken.MODID)
public final class CapabilitySync {

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
    public static void onLivingDeath(LivingDeathEvent event) {
        Fatigue fatigue = event.getEntity().getCapability(CapabilityFatigue.INSTANCE);
        if (fatigue != null) {
            fatigue.setFatigue(0);
            fatigue.setReplenishedFatigue(0);
            fatigue.setExtraFatigueRate(0);
        }
    }

    private static void sync(ServerPlayer player) {
        Fatigue fatigue = player.getCapability(CapabilityFatigue.INSTANCE);
        if (fatigue != null) {
            SomniaNetwork.sendToClient(new FatigueUpdatePacket(fatigue.getFatigue()), player);
        }
    }

    private CapabilitySync() {}
}
