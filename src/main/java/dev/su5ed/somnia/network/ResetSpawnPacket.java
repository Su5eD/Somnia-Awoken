package dev.su5ed.somnia.network;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetSpawnPacket {
    private final boolean resetSpawn;

    public ResetSpawnPacket(boolean resetSpawn) {
        this.resetSpawn = resetSpawn;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.resetSpawn);
    }

    public static ResetSpawnPacket decode(FriendlyByteBuf buf) {
        boolean resetSpawn = buf.readBoolean();
        return new ResetSpawnPacket(resetSpawn);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> props.shouldResetSpawn(this.resetSpawn));
            }
        });
        return true;
    }
}
