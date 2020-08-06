package com.kingrunes.somnia.client.gui;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.StreamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@SideOnly(Side.CLIENT)
public class GuiSomnia extends GuiSleepMP
{
	public final boolean resetSpawn;

	public static final String 	COLOR = new String(new char[]{ (char)167 }),
								BLACK = COLOR+"0",
								WHITE = COLOR+"f",
								RED = COLOR+"c",
								DARK_RED = COLOR+"4",
								GOLD = COLOR+"6";
	
	public static final String	TRANSLATION_FORMAT = "somnia.status.%s",
			SPEED_FORMAT = "%sx%s",
			ETA_FORMAT = WHITE + "(%s:%s)";
	
	public static final byte[]	BYTES_WHITE = new byte[]{ (byte) 255, (byte) 255, (byte) 255 },
								BYTES_DARK_RED = new byte[]{ (byte) 171, 0, 0 },
								BYTES_RED = new byte[]{ (byte) 255, 0, 0 },
								BYTES_GOLD = new byte[]{ (byte) 240, (byte) 200, 30 };
	
	private final RenderItem presetIconRenderer = Minecraft.getMinecraft().getRenderItem();
	private static final ItemStack clockItemStack = new ItemStack(Items.CLOCK);

	private final List<Double> speedValues = new ArrayList<>();
	
	public String status = "Waiting...";
	public double speed = 0;
	public long startTicks = -1L;

	public GuiSomnia() {
		this(true);
	}

	public GuiSomnia(boolean resetSpawn) {
		this.resetSpawn = resetSpawn;
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		if (this.mc.player != null) // save clients from outdated servers causing NPEs
		{
			this.mc.player.wakeUpPlayer(true, true, this.resetSpawn);
		}
	}
	
	@Override
    public void drawScreen(int par1, int par2, float par3)
    {
		super.drawScreen(par1, par2, par3);
		
		boolean currentlySleeping = speed != .0d;
		if (currentlySleeping)
		{
			if (startTicks == -1L)
				startTicks = mc.world.getTotalWorldTime();
		}
		else
			startTicks = -1L;
		

		/*
		 * GL stuff
		 */
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);
		
		/*
		 * Progress bar
		 * Multiplier
		 * ETA
		 * Clock
		 */
		if (startTicks != -1L && Somnia.clientAutoWakeTime != -1)
		{
			// Progress Bar
			this.mc.getTextureManager().bindTexture(Gui.ICONS);
			
			double 	rel = mc.world.getTotalWorldTime()-startTicks,
					diff = Somnia.clientAutoWakeTime-startTicks,
					progress = rel / diff;

			int 	x = 20,
					maxWidth = (this.width-(x*2));
			
			glEnable(GL_BLEND);
			glColor4f(1.0f, 1.0f, 1.0f, .2f);
			renderProgressBar(x, 10, maxWidth, 1.0d);
			
			glDisable(GL_BLEND);
			glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			renderProgressBar(x, 10, maxWidth, progress);
			
			// Multiplier
			renderScaledString(x, 20, 1.5f, SPEED_FORMAT, getColorStringForSpeed(speed), speed);
			
			// ETA
			double total = 0.0d;
			for (double value : speedValues)
				total += value;
			double avg = total / speedValues.size();
			int etaTotalSeconds = (int)((diff-rel) / (avg*20)); // remaining ticks / (average multiplier * standard tick rate)
			
			int etaSeconds = etaTotalSeconds % 60,
				etaMinutes = (etaTotalSeconds-etaSeconds) / 60;
			
			renderScaledString(x + 50 + 10, 20, 1.5f, ETA_FORMAT, (etaMinutes<10?"0":"") + etaMinutes, (etaSeconds<10?"0":"") + etaSeconds);
			
			// Clock
			renderClock(maxWidth - 40, 30, 4.0f);
		}
    }
	
	private void renderProgressBar(int x, int y, int maxWidth, double progress)
	{
		int amount = (int) (progress * maxWidth);
		while (amount > 0)
		{
			this.drawTexturedModalRect(x, y, 0, 69, (Math.min(amount, 180)), 5);
			
			amount -= 180;
			x += 180;
		}
	}
	
	private void renderScaledString(int x, int y, float scale, String format, Object... args)
	{
		String str = String.format(format, args);
		glPushMatrix();
		{
			glTranslatef(x, 20, 0.0f);
			glScalef(scale, scale, 1.0f);
			drawString
			(
				fontRenderer,
				str,
				0,
				0,
				Integer.MIN_VALUE
			);
		}
		glPopMatrix();
		
		//return (int) (fontRendererObj.getStringWidth(str) * scale);
	}
	
	private void renderClock(int x, int y, float scale)
	{
		glPushMatrix();
		{
			glTranslatef(x, y, 0.0f);
			glScalef(scale, scale, 1.0f);
			presetIconRenderer.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, clockItemStack, 0, 0);
		}
		glPopMatrix();
	}
	
	public void readField(DataInputStream in) throws IOException
	{
		switch (in.readByte())
		{
		case 0x00:
			speed = in.readDouble();
			speedValues.add(speed);
			if (speedValues.size() > 5)
				speedValues.remove(0);
			break;
		case 0x01:
			String str = StreamUtils.readString(in);
			status = str.startsWith("f:") ? new TextComponentTranslation(String.format(TRANSLATION_FORMAT, str.substring(2).toLowerCase())).getUnformattedComponentText() : str;
			break;
		}
	}
	
	public static byte[] getColorForSpeed(double speed)
	{
		if (speed < 8)
			return BYTES_WHITE;
		else if (speed < 20)
			return BYTES_DARK_RED;
		else if (speed < 30)
			return BYTES_RED;
		else
			return BYTES_GOLD;
	}
	
	public static String getColorStringForSpeed(double speed)
	{
		if (speed < 8)
			return WHITE;
		else if (speed < 20)
			return DARK_RED;
		else if (speed < 30)
			return RED;
		else
			return GOLD;
	}
}