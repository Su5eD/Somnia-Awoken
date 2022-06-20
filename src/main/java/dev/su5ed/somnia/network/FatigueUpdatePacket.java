package dev.su5ed.somnia.network;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player.getCapability(CapabilityFatigue.INSTANCE)
            .ifPresent(props -> props.setFatigue(this.fatigue))));
        return true;
    }
}
