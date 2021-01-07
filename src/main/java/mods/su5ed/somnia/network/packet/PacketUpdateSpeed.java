package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.client.SomniaClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateSpeed {
    private final double speed;

    public PacketUpdateSpeed(double speed) {
        this.speed = speed;
    }

    public PacketUpdateSpeed(PacketBuffer buffer) {
        this.speed = buffer.readDouble();
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeDouble(this.speed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SomniaClient.clientTickHandler.addSpeedValue(this.speed)));
        return true;
    }
}
