package dev.su5ed.somnia.network.packet;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateFatigue {
    private final double fatigue;

    public PacketUpdateFatigue(double fatigue) {
        this.fatigue = fatigue;
    }

    public PacketUpdateFatigue(PacketBuffer buffer) {
        this.fatigue = buffer.readDouble();
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeDouble(this.fatigue);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
                .ifPresent(props -> props.setFatigue(this.fatigue))));
        return true;
    }
}
