package dev.su5ed.somnia.network.server;

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ActivateBlockPacket {
    private final BlockPos pos;
    private final Direction side;
    private final float hitX;
    private final float hitY;
    private final float hitZ;

    public ActivateBlockPacket(BlockPos pos, Direction side, float hitX, float hitY, float hitZ) {
        this.pos = pos;
        this.side = side;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeEnum(this.side);
        buf.writeFloat(this.hitX);
        buf.writeFloat(this.hitY);
        buf.writeFloat(this.hitZ);
    }

    public static ActivateBlockPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction side = buf.readEnum(Direction.class);
        float hitX = buf.readFloat();
        float hitY = buf.readFloat();
        float hitZ = buf.readFloat();
        return new ActivateBlockPacket(pos, side, hitX, hitY, hitZ);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            BlockState state = player.level().getBlockState(pos);
            BlockHitResult hitResult = new BlockHitResult(new Vec3(this.hitX, this.hitY, this.hitZ), this.side, pos, false);

            state.use(player.level(), player, MoreObjects.firstNonNull(player.swingingArm, InteractionHand.MAIN_HAND), hitResult);
        }
    }
}
