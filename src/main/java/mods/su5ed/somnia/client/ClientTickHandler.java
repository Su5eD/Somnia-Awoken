package mods.su5ed.somnia.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketWakeUpPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class ClientTickHandler {
	private final Minecraft mc = Minecraft.getInstance();
	private final ItemStack clockItemStack = new ItemStack(Items.CLOCK);
	private final List<Double> speedValues = new ArrayList<>();
	public long startTicks = -1L;
	public double speed = 0;

	private boolean muted = false;
	private float defVol;

	public ClientTickHandler() {
		MinecraftForge.EVENT_BUS.register(this);

		CompoundNBT clockNbt = new CompoundNBT();
		clockNbt.putBoolean("quark:clock_calculated", true);
		this.clockItemStack.setTag(clockNbt); //Disables Quark's clock display override
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) tickEnd();
	}

	public void addSpeedValue(double speed) {
		this.speed = speed;
		speedValues.add(speed);
		if (speedValues.size() > 5) speedValues.remove(0);
	}

	public void tickEnd() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		/*
		 * If the player is sleeping and the player has chosen the 'muteSoundWhenSleeping' option in the config,
		 * set the master volume to 0
		 */
		if (mc.player.isSleeping()) {
			if (SomniaConfig.muteSoundWhenSleeping) {
				if (!muted) {
					muted = true;
					defVol = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
					mc.gameSettings.setSoundLevel(SoundCategory.MASTER, .0f);
				}
			}
		} else {
			if (muted) {
				muted = false;
				mc.gameSettings.setSoundLevel(SoundCategory.MASTER, defVol);
			}
		}
		
		/*
		 * Note the isPlayerSleeping() check. Without this, the mod exploits a bug which exists in vanilla Minecraft which
		 * allows the player to teleport back to there bed from anywhere in the world at any time.
		 */
		if (SomniaClient.autoWakeTime > -1 && mc.player.isSleeping() && mc.world.getGameTime() >= SomniaClient.autoWakeTime) {
			SomniaClient.autoWakeTime = -1;
			NetworkHandler.INSTANCE.sendToServer(new PacketWakeUpPlayer());
		}
	}
	
	@SubscribeEvent
	@SuppressWarnings("unused")
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (mc.currentScreen != null && !(mc.currentScreen instanceof IngameMenuScreen)) {
			if (mc.player == null || !mc.player.isSleeping()) return;
		}
		
		FontRenderer fontRenderer = mc.fontRenderer;
		MatrixStack matrixStack = new MatrixStack();
		if (event.phase == TickEvent.Phase.END && !mc.player.isCreative()) {
			if (!mc.player.isSleeping() && !SomniaConfig.fatigueSideEffects && SomniaClient.playerFatigue > SomniaConfig.minimumFatigueToSleep) return;
			String str = String.format(SpeedColor.WHITE.code + "Fatigue: %.2f", SomniaClient.playerFatigue);
			int x, y, stringWidth = fontRenderer.getStringWidth(str);
			int scaledWidth = mc.getMainWindow().getScaledWidth();
			int scaledHeight = mc.getMainWindow().getScaledHeight();
			String param = mc.player.isSleeping() ? "br" : SomniaConfig.displayFatigue.toLowerCase();
			switch (param) {
				case "tc":
					x = (scaledWidth / 2 ) - (stringWidth / 2);
					y = fontRenderer.FONT_HEIGHT;
					break;
				case "tl":
					x = 10;
					y = fontRenderer.FONT_HEIGHT;
					break;
				case "tr":
					x = scaledWidth - stringWidth - 10;
					y = fontRenderer.FONT_HEIGHT;
					break;
				case "bc":
					x = (scaledWidth / 2 ) - (stringWidth / 2);
					y = scaledHeight - fontRenderer.FONT_HEIGHT - 45;
					break;
				case "bl":
					x = 10;
					y = scaledHeight - fontRenderer.FONT_HEIGHT - 10;
					break;
				case "br":
					x = scaledWidth - stringWidth - 10;
					y = scaledHeight - fontRenderer.FONT_HEIGHT - 10;
					break;
				default:
					return;
			}
			fontRenderer.drawString(matrixStack, str, x, y, Integer.MIN_VALUE);
		}

		if (mc.player.isSleeping() && SomniaConfig.somniaGui && SomniaClient.playerFatigue != -1) renderSleepGui(matrixStack, mc.currentScreen);
		else if (startTicks != -1 || speed != 0) {
			this.startTicks = -1;
			this.speed = 0;
		}
	}

	private void renderSleepGui(MatrixStack matrixStack, Screen screen) {
		boolean currentlySleeping = speed != 0;
		if (currentlySleeping) {
			if (startTicks == -1L) startTicks = this.mc.world.getGameTime();
		} else startTicks = -1L;

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
		if (startTicks != -1L && SomniaClient.autoWakeTime != -1) {
			// Progress Bar
			mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);

			double rel = mc.world.getGameTime()-startTicks,
   	    	       diff = SomniaClient.autoWakeTime -startTicks,
                   progress = rel / diff;

			int x = 20,
				maxWidth = (screen.width-(x*2));

			glEnable(GL_BLEND);
			glColor4f(1.0f, 1.0f, 1.0f, .2f);
			renderProgressBar(matrixStack, x, 10, maxWidth, 1.0d);

			glDisable(GL_BLEND);
			glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			renderProgressBar(matrixStack, x, 10, maxWidth, progress);

			// Multiplier
			int offsetX = SomniaConfig.displayETASleep.equals("center") ? screen.width/2 - 80 : SomniaConfig.displayETASleep.equals("right") ? maxWidth - 160 : 0;
			renderScaledString(matrixStack, x + offsetX, 20, 1.5f, "%sx%s", SpeedColor.getColorForSpeed(speed).code, speed);

			// ETA
			double total = 0.0d;
			Double[] values = speedValues.toArray(new Double[0]); //Copy speedValues before iterating over it to prevent a ConcurrentModificationException
			for (double value : values)
				total += value;
			double avg = total / values.length;
			int etaTotalSeconds = (int)((diff-rel) / (avg*20)); // remaining ticks / (average multiplier * standard tick rate)

			int etaSeconds = etaTotalSeconds % 60,
					etaMinutes = (etaTotalSeconds-etaSeconds) / 60;

			renderScaledString(matrixStack, x + 50 + 10 + offsetX, 20, 1.5f, SpeedColor.WHITE.code + "(%s:%s)", (etaMinutes<10?"0":"") + etaMinutes, (etaSeconds<10?"0":"") + etaSeconds);

			// Clock
			renderClock(maxWidth - 40, 30, 4.0f);
		}
	}

	private void renderProgressBar(MatrixStack matrixStack, int x, int y, int maxWidth, double progress) {
		int amount = (int) (progress * maxWidth);
		while (amount > 0)
		{
			if (mc.currentScreen != null) this.mc.currentScreen.blit(matrixStack, x, y, 0, 69, (Math.min(amount, 180)), 5);

			amount -= 180;
			x += 180;
		}
	}

	private void renderScaledString(MatrixStack matrixStack, int x, int y, float scale, String format, Object... args)
	{
		if (mc.currentScreen == null) return;
		String str = String.format(format, args);
		glPushMatrix();
		{
			glTranslatef(x, 20, 0.0f);
			glScalef(scale, scale, 1.0f);
			AbstractGui.drawString
					(
							matrixStack,
							this.mc.fontRenderer,
							str,
							0,
							0,
							Integer.MIN_VALUE
					);
		}
		glPopMatrix();
	}

	private void renderClock(int x, int y, float scale)
	{
		glPushMatrix();
		{
			glTranslatef(x, y, 0.0f);
			glScalef(scale, scale, 1.0f);
			mc.getItemRenderer().renderItemAndEffectIntoGUI(mc.player, clockItemStack, 0, 0);
		}
		glPopMatrix();
	}

	public enum SpeedColor {
		WHITE(SpeedColor.COLOR+"f", 8),
		DARK_RED(SpeedColor.COLOR+"4", 20),
		RED(SpeedColor.COLOR+"c", 30),
		GOLD(SpeedColor.COLOR+"6", 100);

		public static final Set<SpeedColor> VALUES = Arrays.stream(values())
				.sorted(Comparator.comparing(color -> color.range))
				.collect(Collectors.toCollection(LinkedHashSet::new));
		public static final char COLOR = (char) 167;
		public final String code;
		public final double range;

		SpeedColor(String code, double range) {
			this.code = code;
			this.range = range;
		}

		public static SpeedColor getColorForSpeed(double speed) {
			for (SpeedColor color : VALUES) {
				if (speed < color.range) return color;
			}

			return SpeedColor.WHITE;
		}
	}
}