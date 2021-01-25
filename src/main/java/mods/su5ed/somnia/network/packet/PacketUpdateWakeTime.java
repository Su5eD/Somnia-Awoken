package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateWakeTime {
    private final long wakeTime;

    public PacketUpdateWakeTime(long wakeTime) {
        this.wakeTime = wakeTime;
    }

    public PacketUpdateWakeTime(PacketBuffer buffer) {
        this.wakeTime = buffer.readLong();
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeLong(this.wakeTime);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> {
                    props.setWakeTime(this.wakeTime);
                });
            }
        });
        return true;
    }
}
