package dev.su5ed.somnia.network.client;

import dev.su5ed.somnia.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientWakeTimeUpdatePacket {
    private final long wakeTime;

    public ClientWakeTimeUpdatePacket(long wakeTime) {
        this.wakeTime = wakeTime;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(this.wakeTime);
    }

    public static ClientWakeTimeUpdatePacket decode(FriendlyByteBuf buf) {
        long wakeTime = buf.readLong();
        return new ClientWakeTimeUpdatePacket(wakeTime);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.updateWakeTime(this.wakeTime)));
        return true;
    }
}
