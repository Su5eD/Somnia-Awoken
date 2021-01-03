package mods.su5ed.somnia.setup;

import net.minecraft.entity.player.ServerPlayerEntity;

import java.io.DataInputStream;

public class ServerProxy implements IProxy {

    @Override
    public void register() {}

    @Override
    public void handleGUIOpenPacket() {}

    @Override
    public void handlePropUpdatePacket(DataInputStream in) {}

    @Override
    public void handleWakePacket(ServerPlayerEntity player) {
        player.wakeUp();
    }
}
