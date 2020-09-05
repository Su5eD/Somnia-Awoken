package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.network.PacketType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class PacketActivateBlock extends PacketBase {
    private final BlockPos pos;
    private final Direction side;
    private final float x;
    private final float y;
    private final float z;

    public PacketActivateBlock(BlockPos pos, Direction side, float x, float y, float z) {
        super(PacketType.ACTIVATE_BLOCK.ordinal());
        this.pos = pos;
        this.side = side;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeByte(this.id);
        buffer.writeInt(this.pos.getX());
        buffer.writeInt(this.pos.getY());
        buffer.writeInt(this.pos.getZ());
        buffer.writeByte(this.side.ordinal());
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.y);
        buffer.writeFloat(this.z);
    }
}
