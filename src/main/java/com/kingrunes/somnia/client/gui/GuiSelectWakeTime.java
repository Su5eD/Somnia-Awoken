package com.kingrunes.somnia.client.gui;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.RayTraceResult;

public class GuiSelectWakeTime extends GuiScreen
{
	@Override
	public void initGui()
	{
		int i = 0;
		int buttonWidth = 90, buttonHeight = 20;
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width/2)-buttonWidth/2,
				(height/4)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Noon"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*5/8)-buttonWidth/2,
				(height*3/8)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Mid Afternoon"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*3/4)-buttonWidth/2,
				(height/2)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Before Sunset"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*5/8)-buttonWidth/2,
				(height*5/8)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"After Sunset"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width/2)-buttonWidth/2,
				(height*3/4)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Midnight"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*3/8)-buttonWidth/2,
				(height*5/8)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Before Sunrise"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width/4)-buttonWidth/2,
				(height/2)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"After Sunrise"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*3/8)-buttonWidth/2,
				(height*3/8)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Mid Morning"
			)
		);

		/*buttonList.add(
			new GuiButton(
				i++,
				(width/2)-buttonWidth/2,
				(height/7)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Reset spawn: "+(resetSpawn ? "Yes" : "No")
			)
		);*/
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		int i;
		switch (par1GuiButton.id)
		{
		case 0:
			i = 6000;
			break;
		case 1:
			i = 9000;
			break;
		case 2:
			i = 12000;
			break;
		case 3:
			i = 14000;
			break;
		case 4:
			i = 18000;
			break;
		case 5:
			i = 22000;
			break;
		case 6:
			i = 0;
			break;
		case 7:
			i = 3000;
			break;
		default:
			return;
		}
		
		Somnia.clientAutoWakeTime = Somnia.calculateWakeTime(mc.world.getTotalWorldTime(), i);
		/*
		 * Nice little hack to simulate a right click on the bed, don't try this at home kids
		 */
		RayTraceResult mouseOver = mc.objectMouseOver;

		Somnia.eventChannel.sendToServer(PacketHandler.buildRightClickBlockPacket(mouseOver.getBlockPos(), mouseOver.sideHit, (float) mouseOver.hitVec.x, (float) mouseOver.hitVec.y, (float) mouseOver.hitVec.z));

		mc.displayGuiScreen(null);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}