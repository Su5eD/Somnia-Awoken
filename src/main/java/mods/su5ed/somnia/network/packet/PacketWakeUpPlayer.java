package mods.su5ed.somnia.network.packet;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketWakeUpPlayer {
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->  {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) player.stopSleeping();
        });
        return true;
    }
}
