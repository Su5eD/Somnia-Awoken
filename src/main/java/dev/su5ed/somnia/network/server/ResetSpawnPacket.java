package dev.su5ed.somnia.network.server;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import net.minecraft.network.FriendlyByteBuf;
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
        ctx.get().enqueueWork(() -> ctx.get().getSender().getCapability(CapabilityFatigue.INSTANCE)
            .ifPresent(props -> props.setResetSpawn(this.resetSpawn)));
        return true;
    }
}
