package mods.su5ed.somnia.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.ICustomPacket;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

public class NetworkHandler {

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(PacketBuffer buffer, int packetID) {
        ClientPlayNetHandler netHandler = Minecraft.getInstance().getConnection();
        if (netHandler == null) return;
        ICustomPacket<IPacket<?>> packet = NetworkDirection.PLAY_TO_SERVER.buildPacket(Pair.of(buffer, packetID), PacketHandler.CHANNEL_ID);
        netHandler.sendPacket(packet.getThis());
    }

    public static void sendToClient(PacketBuffer buffer, int packetID, ServerPlayerEntity player) {
        ICustomPacket<IPacket<?>> packet = NetworkDirection.PLAY_TO_CLIENT.buildPacket(Pair.of(buffer, packetID), PacketHandler.CHANNEL_ID);
        player.connection.sendPacket(packet.getThis());
    }

    public static void sendToDimension(PacketBuffer buffer, int packetID, ServerWorld world) {
        ICustomPacket<IPacket<?>> packet = NetworkDirection.PLAY_TO_CLIENT.buildPacket(Pair.of(buffer, packetID), PacketHandler.CHANNEL_ID);
        world.getServer().getPlayerList().func_232642_a_(packet.getThis(), world.func_234923_W_());
    }
}
