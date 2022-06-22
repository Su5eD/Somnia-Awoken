package dev.su5ed.somnia.network.server;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> props.setWakeTime(this.wakeTime));
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.updateWakeTime(this.wakeTime));
        });
        return true;
    }
}
