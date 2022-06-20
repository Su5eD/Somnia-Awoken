package dev.su5ed.somnia.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.api.capability.IFatigue;
import dev.su5ed.somnia.core.SomniaConfig;
import dev.su5ed.somnia.network.PlayerWakeUpPacket;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.WakeTimeUpdatePacket;
import dev.su5ed.somnia.util.SideEffectStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ClientTickHandler {
    public static final ClientTickHandler INSTANCE = new ClientTickHandler();
    public static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("0.0");
    private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);
    private static final String TAG_CALCULATED = "quark:clock_calculated";
    
    private final Minecraft mc = Minecraft.getInstance();
    private final List<Double> speedValues = new ArrayList<>();
    
    public long sleepStart = -1;
    public double speed;
    private boolean muted;
    private float volume;

    static {
        //Disable Quark's clock display override
        CLOCK.getOrCreateTag().putBoolean(TAG_CALCULATED, true);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (mc.player.isSleeping() && SomniaConfig.COMMON.muteSoundWhenSleeping.get() && !muted) {
                    muted = true;
                    volume = mc.options.getSoundSourceVolume(SoundSource.MASTER);
                    mc.options.setSoundCategoryVolume(SoundSource.MASTER, 0);
                } else if (muted) {
                    muted = false;
                    mc.options.setSoundCategoryVolume(SoundSource.MASTER, volume);
                }

                mc.player.getCapability(CapabilityFatigue.INSTANCE)
                    .filter(props -> {
                        long wakeTime = props.getWakeTime();
                        return wakeTime > -1 && mc.level.getGameTime() >= wakeTime;
                    })
                    .ifPresent(props -> {
                        SomniaNetwork.INSTANCE.sendToServer(new WakeTimeUpdatePacket(-1));
                        props.setWakeTime(-1);
                        mc.player.stopSleeping();
                        SomniaNetwork.INSTANCE.sendToServer(new PlayerWakeUpPacket());
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
        if (mc.screen != null && !(mc.screen instanceof PauseScreen) && (mc.player == null || !mc.player.isSleeping())) return;
        
        double fatigue = mc.player.getCapability(CapabilityFatigue.INSTANCE)
            .resolve()
            .map(IFatigue::getFatigue)
            .orElse(0.0);
        PoseStack poseStack = new PoseStack();
        if (event.phase == TickEvent.Phase.END && !mc.player.isCreative() && !mc.player.isSpectator() && !mc.options.hideGui) {
            if (!mc.player.isSleeping() && !SomniaConfig.COMMON.fatigueSideEffects.get() && fatigue > SomniaConfig.COMMON.minimumFatigueToSleep.get()) return;
            String str;
            if (SomniaConfig.CLIENT.simpleFatigueDisplay.get()) str = SpeedColor.WHITE.code + SideEffectStage.getSideEffectStageDescription(fatigue);
            else str = String.format(SpeedColor.WHITE.code + "Fatigue: %.2f", fatigue);

            int width = mc.font.width(str),
                scaledWidth = mc.getWindow().getGuiScaledWidth(),
                scaledHeight = mc.getWindow().getGuiScaledHeight();
            FatigueDisplayPosition pos = mc.player.isSleeping() ? FatigueDisplayPosition.BOTTOM_RIGHT : FatigueDisplayPosition.valueOf(SomniaConfig.CLIENT.displayFatigue.get());
            mc.font.draw(poseStack, str, pos.getX(scaledWidth, width), pos.getY(scaledHeight, mc.font.lineHeight), Integer.MIN_VALUE);
        }

        if (mc.player.isSleeping() && SomniaConfig.CLIENT.somniaGui.get() && fatigue != -1) renderSleepGui(poseStack, mc.screen);
        else if (sleepStart != -1 || speed != 0) {
            this.sleepStart = -1;
            this.speed = 0;
        }
    }

    private void renderSleepGui(PoseStack poseStack, Screen screen) {
        if (screen == null) return;

        if (speed != 0) {
            if (sleepStart == -1) sleepStart = this.mc.level.getGameTime();
        } else sleepStart = -1;

        mc.player.getCapability(CapabilityFatigue.INSTANCE)
            .map(IFatigue::getWakeTime)
            .filter(wakeTime -> wakeTime > -1)
            .ifPresent(wakeTime -> {
                if (sleepStart != -1) {
                    mc.getTextureManager().getTexture(GuiComponent.GUI_ICONS_LOCATION).bind();

                    double sleepDuration = mc.level.getGameTime() - sleepStart,
                        remaining = wakeTime - sleepStart,
                        progress = sleepDuration / remaining;

                    int width = screen.width - 40;

                    RenderSystem.enableBlend();
//                    glColor4f(1, 1, 1, 0.2F); TODO
                    renderProgressBar(poseStack, width, 1);

                    RenderSystem.disableBlend();
                    renderProgressBar(poseStack, width, progress);

                    String displayETASleep = SomniaConfig.CLIENT.displayETASleep.get();
                    int offsetX = displayETASleep.equals("center") ? screen.width / 2 - 80 : displayETASleep.equals("right") ? width - 160 : 0;
                    renderScaledString(poseStack, offsetX + 20, String.format("%sx%s", SpeedColor.getColorForSpeed(speed).code, MULTIPLIER_FORMAT.format(speed)));

                    double average = speedValues.stream()
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .summaryStatistics()
                        .getAverage();
                    long eta = Math.round((remaining - sleepDuration) / (average * 20));

                    renderScaledString(poseStack, offsetX + 80, getETAString(eta));

                    renderClock(poseStack, width);
                }
            });
    }

    private String getETAString(long totalSeconds) {
        long etaSeconds = totalSeconds % 60, etaMinutes = (totalSeconds - etaSeconds) / 60;
        return String.format(SpeedColor.WHITE.code + "(%s:%s)", (etaMinutes < 10 ? "0" : "") + etaMinutes, (etaSeconds < 10 ? "0" : "") + etaSeconds);
    }

    private void renderProgressBar(PoseStack poseStack, int width, double progress) {
        int x = 20;
        for (int amount = (int) (progress * width); amount > 0; amount -= 180, x += 180) {
            if (mc.screen != null) this.mc.screen.blit(poseStack, x, 10, 0, 69, Math.min(amount, 180), 5);
        }
    }

    private void renderScaledString(PoseStack poseStack, int x, String str) {
        if (mc.screen == null) return;
        
        poseStack.pushPose();
        poseStack.translate(x, 20, 0);
        poseStack.scale(1.5F, 1.5F, 1);
        mc.font.drawShadow(poseStack, str, 0, 0, Integer.MIN_VALUE);
        poseStack.popPose();
    }

    private void renderClock(PoseStack poseStack, int maxWidth) {
        int x = switch (SomniaConfig.CLIENT.somniaGuiClockPosition.get()) {
            case "left" -> 40;
            case "center" -> maxWidth / 2;
            default -> maxWidth - 40;
        };
        poseStack.pushPose();
        poseStack.translate(x, 35, 0);
        poseStack.scale(4, 4, 1);
        mc.getItemRenderer().renderAndDecorateItem(CLOCK, 0, 0);
        poseStack.popPose();
    }

    public enum SpeedColor {
        WHITE(SpeedColor.COLOR + "f", 8),
        DARK_RED(SpeedColor.COLOR + "4", 20),
        RED(SpeedColor.COLOR + "c", 30),
        GOLD(SpeedColor.COLOR + "6", 100);

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