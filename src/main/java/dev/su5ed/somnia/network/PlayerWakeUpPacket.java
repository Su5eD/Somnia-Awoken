package dev.su5ed.somnia.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerWakeUpPacket {
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->  {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) player.stopSleeping();
        });
        return true;
    }
}
