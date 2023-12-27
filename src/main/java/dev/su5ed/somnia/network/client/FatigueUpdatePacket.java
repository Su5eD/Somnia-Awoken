package dev.su5ed.somnia.network.client;

import dev.su5ed.somnia.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.NetworkEvent;

public class FatigueUpdatePacket {
    private final double fatigue;

    public FatigueUpdatePacket(double fatigue) {
        this.fatigue = fatigue;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(this.fatigue);
    }

    public static FatigueUpdatePacket decode(FriendlyByteBuf buf) {
        double fatigue = buf.readDouble();
        return new FatigueUpdatePacket(fatigue);
    }

    public void handle(NetworkEvent.Context ctx) {
        if (FMLLoader.getDist().isClient()) {
            ClientPacketHandler.updateFatigue(this.fatigue);
        }
    }
}
