package dev.su5ed.somnia.network.server;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.Fatigue;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

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

    public void handle(NetworkEvent.Context ctx) {
        Fatigue fatigue = ctx.getSender().getCapability(CapabilityFatigue.INSTANCE);
        if (fatigue != null) {
            fatigue.setResetSpawn(this.resetSpawn);
        }
    }
}
