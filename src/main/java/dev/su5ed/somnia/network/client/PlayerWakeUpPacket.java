package dev.su5ed.somnia.network.client;

import dev.su5ed.somnia.network.ClientPacketHandler;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.NetworkEvent;

public class PlayerWakeUpPacket {
    public void handle(NetworkEvent.Context ctx) {
        if (FMLLoader.getDist().isClient()) {
            ClientPacketHandler.wakeUpPlayer();
        }
    }
}
