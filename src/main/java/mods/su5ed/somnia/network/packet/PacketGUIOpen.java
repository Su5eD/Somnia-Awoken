package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.network.PacketType;
import net.minecraft.network.PacketBuffer;

public class PacketGUIOpen extends PacketBase {

    public PacketGUIOpen() {
        super(PacketType.OPEN_GUI.ordinal());
    }

    @Override
    public void write(PacketBuffer buffer) {}
}
