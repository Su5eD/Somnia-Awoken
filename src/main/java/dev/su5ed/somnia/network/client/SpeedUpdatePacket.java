package dev.su5ed.somnia.network.client;

import dev.su5ed.somnia.ClientSleepHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.NetworkEvent;

public class SpeedUpdatePacket {
    private final double speed;

    public SpeedUpdatePacket(double speed) {
        this.speed = speed;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(this.speed);
    }

    public static SpeedUpdatePacket decode(FriendlyByteBuf buf) {
        double speed = buf.readDouble();
        return new SpeedUpdatePacket(speed);
    }

    public void handle(NetworkEvent.Context ctx) {
        if (FMLLoader.getDist().isClient()) {
            ClientSleepHandler.INSTANCE.addSpeedValue(this.speed);
        }
    }
}
