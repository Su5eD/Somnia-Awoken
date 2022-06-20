package dev.su5ed.somnia.network.packet;

import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketActivateBlock {
    private final BlockPos pos;
    private final Direction side;
    private final float hitX;
    private final float hitY;
    private final float hitZ;

    public PacketActivateBlock(BlockPos pos, Direction side, float hitX, float hitY, float hitZ) {
        this.pos = pos;
        this.side = side;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    public PacketActivateBlock(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
        this.side = Direction.from3DDataValue(buffer.readByte());
        this.hitX = buffer.readFloat();
        this.hitY = buffer.readFloat();
        this.hitZ = buffer.readFloat();
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeByte(this.side.get3DDataValue());
        buffer.writeFloat(this.hitX);
        buffer.writeFloat(this.hitY);
        buffer.writeFloat(this.hitZ);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                BlockState state = player.level.getBlockState(pos);
                BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(new Vector3d(this.hitX, this.hitY, this.hitZ), this.side, pos, false);

                state.use(player.level, player, MoreObjects.firstNonNull(player.swingingArm, Hand.MAIN_HAND), rayTraceResult);
            }
        });
        return true;
    }
}
