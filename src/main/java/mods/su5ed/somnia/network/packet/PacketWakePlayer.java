package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.network.PacketType;
import net.minecraft.network.PacketBuffer;

public class PacketWakePlayer extends PacketBase {

    public PacketWakePlayer() {
        super(PacketType.WAKE_PLAYER.ordinal());
    }

    @Override
    public void write(PacketBuffer buffer) {}
}
