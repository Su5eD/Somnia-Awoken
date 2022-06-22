package dev.su5ed.somnia.network.server;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WakeTimeUpdatePacket {
    private final long wakeTime;

    public WakeTimeUpdatePacket(long wakeTime) {
        this.wakeTime = wakeTime;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(this.wakeTime);
    }

    public static WakeTimeUpdatePacket decode(FriendlyByteBuf buf) {
        long wakeTime = buf.readLong();
        return new WakeTimeUpdatePacket(wakeTime);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ctx.get().getSender().getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> props.setWakeTime(this.wakeTime)));
        return true;
    }
}
