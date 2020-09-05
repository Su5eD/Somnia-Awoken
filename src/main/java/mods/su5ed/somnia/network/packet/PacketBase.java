package mods.su5ed.somnia.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.tuple.Pair;

public abstract class PacketBase {
    protected final int id;

    protected PacketBase(int id) {
        this.id = id;
    }

    public Pair<PacketBuffer, Integer> buildPacket() {
        PacketBuffer buffer = unpooled();
        buffer.writeByte(this.id);
        write(buffer);
        return Pair.of(buffer, this.id);
    }

    public abstract void write(PacketBuffer buffer);

    private static PacketBuffer unpooled()
    {
        return new PacketBuffer(Unpooled.buffer());
    }
}
