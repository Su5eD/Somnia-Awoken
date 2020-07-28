package com.kingrunes.somnia.client;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.client.gui.GuiSomnia;
import com.kingrunes.somnia.common.CommonProxy;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static double playerFatigue = -1;
	
	@Override
	public void register()
	{
		super.register();
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(new ClientTickHandler());
	}

	@SubscribeEvent
	public void interactHook(PlayerInteractEvent event)
	{
		World world = event.getWorld();
		if (!world.isRemote)
			return;

		BlockPos pos = event.getPos();
		
		if (pos == null)
			return;

		EntityPlayer player = event.getEntityPlayer();
		IBlockState state = world.getBlockState(pos);
		
		if (event instanceof PlayerInteractEvent.RightClickBlock && state.getBlock() == Blocks.BED)
		{
//			int i1 = event.entity.worldObj.getBlockMetadata(event.x, event.y, event.z);
//			int j1 = i1 & 3;
			
			if (state.getValue(BlockBed.PART) != BlockBed.EnumPartType.HEAD)
            {
                pos = pos.offset(state.getValue(BlockBed.FACING));
                state = world.getBlockState(pos);

                if (state.getBlock() != Blocks.BED)
                {
                    event.setCanceled(true);
                    return;
                }
            }
			
			if (Math.abs(player.posX - (double)pos.getX()) < 3.0D && Math.abs(player.posY - (double)pos.getY()) < 2.0D && Math.abs(player.posZ - (double)pos.getZ()) < 3.0D)
			{
				ItemStack currentItem = player.inventory.getCurrentItem();
				if (currentItem != null && currentItem.getItem() == Items.CLOCK)
				{
					event.setCanceled(true);
					Minecraft.getMinecraft().displayGuiScreen(new GuiSelectWakeTime());
				}
				else
				{
					// Wake at next sunrise/sunset (whichever comes first)
					long totalWorldTime = world.getTotalWorldTime();
					Somnia.clientAutoWakeTime = Somnia.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
				}
			}
		}
		else if (Minecraft.getMinecraft().currentScreen instanceof GuiSelectWakeTime)
			event.setCanceled(true);
	}
	
	@Override
	public void handleGUIOpenPacket()
	{
		if (somniaGui)
		{
			final Minecraft mc = Minecraft.getMinecraft();
			
			mc.addScheduledTask(new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					mc.displayGuiScreen(new GuiSomnia());
					return null;
				}
			});
		}
	}

	@Override
	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{
		byte target = in.readByte();
		
		switch (target)
		{
		case 0x00:
			GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
			if (currentScreen != null && currentScreen instanceof GuiSomnia)
			{
				GuiSomnia gui = (GuiSomnia)currentScreen;
				
				int b = in.readInt();
				for (int a=0; a<b; a++)
					gui.readField(in);
			}
			break;
		case 0x01:
			int b = in.readInt();
			for (int a=0; a<b; a++)
			{
				switch (in.readByte())
				{
				case 0x00:
					playerFatigue = in.readDouble();
					break;
				}
			}
			break;
		}
	}

	@Override
	public void handleGUIClosePacket(EntityPlayerMP player)
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
		player.wakeUpPlayer(true, true, true);
	}
}