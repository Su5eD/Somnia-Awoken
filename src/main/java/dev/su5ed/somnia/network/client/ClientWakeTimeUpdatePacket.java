package dev.su5ed.somnia.network.client;

import dev.su5ed.somnia.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.NetworkEvent;

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

    public void handle(NetworkEvent.Context ctx) {
        if (FMLLoader.getDist().isClient()) {
            ClientPacketHandler.updateWakeTime(this.wakeTime);
        }
    }
}
