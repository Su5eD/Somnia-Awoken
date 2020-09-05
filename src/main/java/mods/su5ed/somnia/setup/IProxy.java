package mods.su5ed.somnia.setup;

import net.minecraft.entity.player.ServerPlayerEntity;

import java.io.DataInputStream;
import java.io.IOException;

public interface IProxy {

    void register();

    void handleGUIOpenPacket();

    void handlePropUpdatePacket(DataInputStream in) throws IOException;

    void handleWakePacket(ServerPlayerEntity player);
}
