package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.api.capability.FatigueCapability;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketResetSpawn {
    private final boolean resetSpawn;

    public PacketResetSpawn(boolean resetSpawn) {
        this.resetSpawn = resetSpawn;
    }

    public PacketResetSpawn(PacketBuffer buffer) {
        this.resetSpawn = buffer.readBoolean();
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeBoolean(this.resetSpawn);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null).ifPresent(props -> props.shouldResetSpawn(this.resetSpawn));
            }
        });
        return true;
    }
}
