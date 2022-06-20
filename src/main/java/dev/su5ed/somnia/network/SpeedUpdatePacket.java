package dev.su5ed.somnia.network;

import dev.su5ed.somnia.handler.ClientTickHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTickHandler.INSTANCE.addSpeedValue(this.speed)));
        return true;
    }
}
