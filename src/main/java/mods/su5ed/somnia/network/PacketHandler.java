package mods.su5ed.somnia.network;

import io.netty.buffer.ByteBufInputStream;
import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketHandler
{
	public static final ResourceLocation CHANNEL_ID = new ResourceLocation(Somnia.MOD_ID, "channel");
	/*
	 * Handling
	 */
	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onServerPacket(NetworkEvent.ServerCustomPayloadEvent event)
	{
		onPacket(event.getPayload(), event.getSource().get().getSender());
	}
	
	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onClientPacket(NetworkEvent.ClientCustomPayloadEvent event)
	{
		onPacket(event.getPayload(), event.getSource().get().getSender());
	}
	
	public void onPacket(PacketBuffer payload, ServerPlayerEntity player)
	{
		DataInputStream in = new DataInputStream(new ByteBufInputStream(payload));
		try
		{
			byte id = in.readByte();
			
			switch (id)
			{
				case 0x00:
					handleGUIOpenPacket();
					break;
				case 0x01:
					handleWakePacket(player);
					break;
				case 0x02:
					handlePropUpdatePacket(in, player);
					break;
				case 0x03:
					handleRightClickBlockPacket(player, in);
					break;
				case 0x04:
					handleRideEntityPacket(player, in);
					break;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// CLIENT
	private void handleGUIOpenPacket() {
		Somnia.proxy.handleGUIOpenPacket();
	}
	
	private void handlePropUpdatePacket(DataInputStream in, @Nullable ServerPlayerEntity player) throws IOException {
		if (player == null || player.world.isRemote) {
			Somnia.proxy.handlePropUpdatePacket(in);
			return;
		}
		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(props -> {
			try {
				byte target = in.readByte();
				if (target == 0x01) {
					int b = in.readInt();
					for (int a=0; a<b; a++)
					{
						if (in.readByte() == 0x01) {
							props.shouldResetSpawn(in.readBoolean());
						}
					}
				}
			} catch(IOException ignored) {}
		});
	}
	
	
	private void handleWakePacket(ServerPlayerEntity player)
	{
		Somnia.proxy.handleWakePacket(player);
	}

	@SuppressWarnings("deprecation")
	private void handleRightClickBlockPacket(ServerPlayerEntity player, DataInputStream in) throws IOException {
		BlockPos pos = new BlockPos(in.readInt(), in.readInt(), in.readInt());
		Direction direction = Direction.values()[in.readByte()];
		BlockState state = player.world.getBlockState(pos);
		BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(new Vector3d(in.readFloat(), in.readFloat(), in.readFloat()), direction, pos, false);

		state.getBlock().onBlockActivated(state, player.world, pos, player, Hand.MAIN_HAND, rayTraceResult);
	}

	private void handleRideEntityPacket(ServerPlayerEntity player, DataInputStream in) throws IOException {
		Entity entity = player.world.getEntityByID(in.readInt());
		if (entity == null) return;

		player.startRiding(entity, true);
	}

}