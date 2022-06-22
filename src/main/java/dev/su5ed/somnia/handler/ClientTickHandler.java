package dev.su5ed.somnia.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.IFatigue;
import dev.su5ed.somnia.SomniaConfig;
import dev.su5ed.somnia.util.FatigueDisplayPosition;
import dev.su5ed.somnia.util.RenderHelper;
import dev.su5ed.somnia.util.SideEffectStage;
import dev.su5ed.somnia.util.SpeedColor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientTickHandler {
    public static final ClientTickHandler INSTANCE = new ClientTickHandler();
    
    private static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("0.0");
    private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);
    
    private final Minecraft mc = Minecraft.getInstance();
    private final List<Double> speedValues = new ArrayList<>();
    
    public long sleepStart = -1;
    public double speed;
    
    private boolean muted;
    private float volume;

    static {
        //Disable Quark's clock display override
        CLOCK.getOrCreateTag().putBoolean("quark:clock_calculated", true);
    }
    
    public void addSpeedValue(double speed) {
        this.speed = speed;
        this.speedValues.add(speed);
        if (this.speedValues.size() > 5) this.speedValues.remove(0);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && mc.player != null) {
            if (mc.player.isSleeping() && SomniaConfig.COMMON.muteSoundWhenSleeping.get() && !muted) {
                muted = true;
                volume = mc.options.getSoundSourceVolume(SoundSource.MASTER);
                mc.options.setSoundCategoryVolume(SoundSource.MASTER, 0);
            } else if (muted) {
                muted = false;
                mc.options.setSoundCategoryVolume(SoundSource.MASTER, volume);
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (mc.screen != null && !(mc.screen instanceof PauseScreen) && (mc.player == null || !mc.player.isSleeping())) return;
        
        PoseStack poseStack = new PoseStack();
        IFatigue fatigue = mc.player.getCapability(CapabilityFatigue.INSTANCE).orElse(null);
        if (fatigue == null) return;
        
        double fatigueAmount = fatigue.getFatigue();
        if (event.phase == TickEvent.Phase.END && !mc.player.isCreative() && !mc.player.isSpectator() && !mc.options.hideGui) {
            if (!mc.player.isSleeping() && !SomniaConfig.COMMON.fatigueSideEffects.get() && fatigueAmount > SomniaConfig.COMMON.minimumFatigueToSleep.get()) return;
            
            String str = SomniaConfig.CLIENT.simpleFatigueDisplay.get() ? SpeedColor.WHITE.color + SideEffectStage.getSideEffectStageDescription(fatigueAmount)
                : String.format("%sFatigue: %.2f", SpeedColor.WHITE.color, fatigueAmount);

            int width = mc.font.width(str);
            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            int scaledHeight = mc.getWindow().getGuiScaledHeight();
            FatigueDisplayPosition pos = mc.player.isSleeping() ? FatigueDisplayPosition.BOTTOM_RIGHT : SomniaConfig.CLIENT.getFatigueDisplayPos();
            mc.font.draw(poseStack, str, pos.getX(scaledWidth, width), pos.getY(scaledHeight, mc.font.lineHeight), Integer.MIN_VALUE);
        }

        if (mc.player.isSleeping() && SomniaConfig.CLIENT.somniaGui.get() && fatigueAmount != -1) {
            if (mc.screen != null) renderSleepGui(poseStack, mc.screen, fatigue);
        }
        else if (this.sleepStart != -1 || this.speed != 0) {
            this.sleepStart = -1;
            this.speed = 0;
        }
    }

    private void renderSleepGui(PoseStack poseStack, Screen screen, IFatigue fatigue) {
        if (speed != 0) {
            if (sleepStart == -1) sleepStart = this.mc.level.getGameTime();
        }
        else sleepStart = -1;

        if (sleepStart != -1) {
            long wakeTime = fatigue.getWakeTime();
            double sleepDuration = mc.level.getGameTime() - sleepStart,
                remaining = wakeTime - sleepStart,
                progress = sleepDuration / remaining;
            int width = screen.width - 40;

            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            renderProgressBar(screen, poseStack, width, progress);

            String displayETASleep = SomniaConfig.CLIENT.displayETASleep.get();
            int offsetX = displayETASleep.equals("center") ? screen.width / 2 - 80 : displayETASleep.equals("right") ? width - 160 : 0;
            renderScaledString(poseStack, offsetX + 20, String.format("%sx%s", SpeedColor.getColorForSpeed(speed).color, MULTIPLIER_FORMAT.format(speed)));

            double average = speedValues.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics()
                .getAverage();
            long eta = Math.round((remaining - sleepDuration) / (average * 20));

            renderScaledString(poseStack, offsetX + 80, getETAString(eta));

            renderClock(poseStack, width);
        }
    }

    private String getETAString(long totalSeconds) {
        long etaSeconds = totalSeconds % 60;
        long etaMinutes = (totalSeconds - etaSeconds) / 60;
        return String.format(SpeedColor.WHITE.color + "(%s:%s)", (etaMinutes < 10 ? "0" : "") + etaMinutes, (etaSeconds < 10 ? "0" : "") + etaSeconds);
    }

    private void renderProgressBar(Screen screen, PoseStack poseStack, int width, double progress) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 0.2F);
        doRenderProgressBar(screen, poseStack, width, 1);
        
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        doRenderProgressBar(screen, poseStack, width, progress);
    }
    
    private void doRenderProgressBar(Screen screen, PoseStack poseStack, int width, double progress) {
        for (int amount = (int) (progress * width), x = 20; amount > 0; amount -= 180, x += 180) {
            screen.blit(poseStack, x, 10, 0, 69, Math.min(amount, 180), 5);
        }
    }

    private void renderScaledString(PoseStack poseStack, int x, String str) {
        poseStack.pushPose();
        poseStack.translate(x, 20, 0);
        poseStack.scale(1.5F, 1.5F, 1);
        mc.font.drawShadow(poseStack, str, 0, 0, Integer.MIN_VALUE);
        poseStack.popPose();
    }

    private void renderClock(PoseStack poseStack, int width) {
        int x = switch (SomniaConfig.CLIENT.somniaGuiClockPosition.get()) {
            case "left" -> 40;
            case "center" -> width / 2;
            default -> width - 40;
        };
        poseStack.pushPose();
        poseStack.translate(x, 35, 0);
        poseStack.scale(4, 4, 1);
        RenderHelper.renderItemStackInGui(poseStack, CLOCK, 0, 0);
        poseStack.popPose();
    }
}