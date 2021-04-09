package mods.su5ed.somnia.handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketUpdateWakeTime;
import mods.su5ed.somnia.network.packet.PacketWakeUpPlayer;
import mods.su5ed.somnia.util.SideEffectStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class ClientTickHandler {
	public static final ClientTickHandler INSTANCE = new ClientTickHandler();
	public static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("0.0");
	private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);
	private final Minecraft mc = Minecraft.getInstance();
	private final List<Double> speedValues = new ArrayList<>();
	public long sleepStart = -1;
	public double speed;
	private boolean muted;
	private float volume;

	static {
		CompoundNBT clockNbt = new CompoundNBT();
		clockNbt.putBoolean("quark:clock_calculated", true);
		CLOCK.setTag(clockNbt); //Disables Quark's clock display override
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				if (mc.player.isSleeping() && SomniaConfig.muteSoundWhenSleeping && !muted) {
					muted = true;
					volume = mc.options.getSoundSourceVolume(SoundCategory.MASTER);
					mc.options.setSoundCategoryVolume(SoundCategory.MASTER, 0);
				} else if (muted) {
					muted = false;
					mc.options.setSoundCategoryVolume(SoundCategory.MASTER, volume);
				}

				mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
						.filter(props -> {
							long wakeTime = props.getWakeTime();
							return wakeTime > -1 && mc.level.getGameTime() >= wakeTime;
						})
						.ifPresent(props -> {
							NetworkHandler.INSTANCE.sendToServer(new PacketUpdateWakeTime(-1));
							props.setWakeTime(-1);
							mc.player.stopSleeping();
							NetworkHandler.INSTANCE.sendToServer(new PacketWakeUpPlayer());
						});
			}
		}
	}

	public void addSpeedValue(double speed) {
		this.speed = speed;
		speedValues.add(speed);
		if (speedValues.size() > 5) speedValues.remove(0);
	}
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (mc.screen != null && !(mc.screen instanceof IngameMenuScreen)) {
			if (mc.player == null || !mc.player.isSleeping()) return;
		}

		double fatigue = mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
				.resolve()
				.map(IFatigue::getFatigue)
				.orElse(0D);
		MatrixStack matrixStack = new MatrixStack();
		if (event.phase == TickEvent.Phase.END && !mc.player.isCreative() && !mc.player.isSpectator() && !mc.options.hideGui) {
			if (!mc.player.isSleeping() && !SomniaConfig.fatigueSideEffects && fatigue > SomniaConfig.minimumFatigueToSleep) return;
			String str;
			if (SomniaConfig.simpleFatigueDisplay) str = SpeedColor.WHITE.code + SideEffectStage.getSideEffectStageDescription(fatigue);
			else str = String.format(SpeedColor.WHITE.code + "Fatigue: %.2f", fatigue);

			int width = mc.font.width(str),
				scaledWidth = mc.getWindow().getGuiScaledWidth(),
				scaledHeight = mc.getWindow().getGuiScaledHeight();
			FatigueDisplayPosition pos = mc.player.isSleeping() ? FatigueDisplayPosition.BOTTOM_RIGHT : FatigueDisplayPosition.valueOf(SomniaConfig.displayFatigue);
			mc.font.draw(matrixStack, str, pos.getX(scaledWidth, width), pos.getY(scaledHeight, mc.font.lineHeight), Integer.MIN_VALUE);
		}

		if (mc.player.isSleeping() && SomniaConfig.somniaGui && fatigue != -1) renderSleepGui(matrixStack, mc.screen);
		else if (sleepStart != -1 || speed != 0) {
			this.sleepStart = -1;
			this.speed = 0;
		}
	}

	private void renderSleepGui(MatrixStack matrixStack, Screen screen) {
		if (screen == null) return;

		if (speed != 0) {
			if (sleepStart == -1) sleepStart = this.mc.level.getGameTime();
		} else sleepStart = -1;

		glColor4f(1, 1, 1, 1);
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);

		mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
				.map(IFatigue::getWakeTime)
				.filter(wakeTime -> wakeTime > -1)
				.ifPresent(wakeTime -> {
					if (sleepStart != -1) {
						mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);

						double sleepDuration = mc.level.getGameTime() - sleepStart,
								remaining = wakeTime - sleepStart,
								progress = sleepDuration / remaining;

						int width = screen.width - 40;

						glEnable(GL_BLEND);
						glColor4f(1, 1, 1, 0.2F);
						renderProgressBar(matrixStack, width, 1);

						glDisable(GL_BLEND);
						glColor4f(1, 1, 1, 1);
						renderProgressBar(matrixStack, width, progress);

						int offsetX = SomniaConfig.displayETASleep.equals("center") ? screen.width/2 - 80 : SomniaConfig.displayETASleep.equals("right") ? width - 160 : 0;
						renderScaledString(matrixStack, offsetX + 20, String.format("%sx%s", SpeedColor.getColorForSpeed(speed).code, MULTIPLIER_FORMAT.format(speed)));

						double average = speedValues.stream()
								.filter(Objects::nonNull)
								.mapToDouble(Double::doubleValue)
								.summaryStatistics()
								.getAverage();
						long eta = Math.round((remaining - sleepDuration) / (average * 20));

						renderScaledString(matrixStack, offsetX + 80, getETAString(eta));

						renderClock(width);
					}
				});
	}

	private String getETAString(long totalSeconds) {
		long etaSeconds = totalSeconds % 60, etaMinutes = (totalSeconds - etaSeconds) / 60;
		return String.format(SpeedColor.WHITE.code + "(%s:%s)", (etaMinutes<10?"0":"") + etaMinutes, (etaSeconds<10?"0":"") + etaSeconds);
	}

	private void renderProgressBar(MatrixStack matrixStack, int width, double progress) {
		int x = 20;
		for (int amount = (int) (progress * width); amount > 0; amount -= 180, x += 180) {
			if (mc.screen != null) this.mc.screen.blit(matrixStack, x, 10, 0, 69, Math.min(amount, 180), 5);
		}
	}

	private void renderScaledString(MatrixStack matrixStack, int x, String str) {
		if (mc.screen == null) return;
		glPushMatrix();
		glTranslatef(x, 20, 0);
		glScalef(1.5F, 1.5F, 1);
		mc.font.drawShadow(matrixStack, str, 0, 0, Integer.MIN_VALUE);
		glPopMatrix();
	}

	private void renderClock(int maxWidth) {
		int x;
		switch (SomniaConfig.somniaGuiClockPosition) {
			case "left":
				x = 40;
				break;
			case "center":
				x = maxWidth / 2;
				break;
			default:
			case "right":
				x = maxWidth - 40;
				break;
		}
		glPushMatrix();
		glTranslatef(x, 35, 0);
		glScalef(4, 4, 1);
		mc.getItemRenderer().renderAndDecorateItem(mc.player, CLOCK, 0, 0);
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

	@SuppressWarnings("unused")
	public enum FatigueDisplayPosition {
		TOP_CENTER((scaledWidth, stringWidth) -> scaledWidth / 2 - stringWidth / 2, (scaledHeight, fontHeight) -> fontHeight),
		TOP_LEFT((scaledWidth, stringWidth) -> 10, (scaledHeight, fontHeight) -> fontHeight),
		TOP_RIGHT((scaledWidth, stringWidth) -> scaledWidth - stringWidth - 10, (scaledHeight, fontHeight) -> fontHeight),
		BOTTOM_CENTER((scaledWidth, stringWidth) -> scaledWidth / 2 - stringWidth / 2, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 45),
		BOTTOM_LEFT((scaledWidth, stringWidth) -> 10, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 10),
		BOTTOM_RIGHT((scaledWidth, stringWidth) -> scaledWidth - stringWidth - 10, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 10);
		private final BiFunction<Integer, Integer, Integer> x;
		private final BiFunction<Integer, Integer, Integer> y;

		FatigueDisplayPosition(BiFunction<Integer, Integer, Integer> x, BiFunction<Integer, Integer, Integer> y) {
			this.x = x;
			this.y = y;
		}

		public int getX(int scaledWidth, int stringWidth) {
			return this.x.apply(scaledWidth, stringWidth);
		}

		public int getY(int scaledHeight, int fontHeight) {
			return this.y.apply(scaledHeight, fontHeight);
		}
	}
}