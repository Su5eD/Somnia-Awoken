package dev.su5ed.somnia.network.server;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.Fatigue;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

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

    public void handle(NetworkEvent.Context ctx) {
        Fatigue fatigue = ctx.getSender().getCapability(CapabilityFatigue.INSTANCE);
        if (fatigue != null) {
            fatigue.setWakeTime(this.wakeTime);
        }
    }
}
