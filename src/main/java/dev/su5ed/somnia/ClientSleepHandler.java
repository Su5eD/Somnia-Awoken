package dev.su5ed.somnia;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.Fatigue;
import dev.su5ed.somnia.util.FatigueDisplayPosition;
import dev.su5ed.somnia.util.RenderHelper;
import dev.su5ed.somnia.util.SideEffectStage;
import dev.su5ed.somnia.util.SpeedColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class ClientSleepHandler {
    public static final ClientSleepHandler INSTANCE = new ClientSleepHandler();
    
    private static final DecimalFormat FATIGUE_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("0.0");
    private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);
    
    private final Minecraft mc = Minecraft.getInstance();
    private final Deque<Double> speedValues = new ArrayDeque<>();
    
    public long sleepStartTime = -1;
    
    private boolean muted;
    private float previousVolume;

    static {
        //Disable Quark's clock display override
        CLOCK.getOrCreateTag().putBoolean("quark:clock_calculated", true);
    }
    
    public void addSpeedValue(double speed) {
        this.speedValues.add(speed);
        if (this.speedValues.size() > 5) this.speedValues.removeFirst();
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && this.mc.player != null) {
            if (this.mc.player.isSleeping() && SomniaConfig.COMMON.muteSoundWhenSleeping.get() && !this.muted) {
                this.muted = true;
                this.previousVolume = this.mc.options.getSoundSourceVolume(SoundSource.MASTER);
                this.mc.options.setSoundCategoryVolume(SoundSource.MASTER, 0);
            } else if (muted) {
                this.muted = false;
                this.mc.options.setSoundCategoryVolume(SoundSource.MASTER, this.previousVolume);
            }
        }
    }
    
    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        this.sleepStartTime = -1;
        this.speedValues.clear();
    }
    
    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (this.mc.screen != null && !(this.mc.screen instanceof PauseScreen) && (this.mc.player == null || !this.mc.player.isSleeping())) return;
        
        this.mc.player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(fatigue -> {
            PoseStack poseStack = new PoseStack();
            double fatigueAmount = fatigue.getFatigue();
            if (event.phase == TickEvent.Phase.END && !this.mc.player.isCreative() && !this.mc.player.isSpectator() && !this.mc.options.hideGui) {
                if (!this.mc.player.isSleeping() && !SomniaConfig.COMMON.fatigueSideEffects.get() && fatigueAmount > SomniaConfig.COMMON.minimumFatigueToSleep.get()) return;
                
                renderFatigueDisplay(poseStack, fatigueAmount);
            }

            if (this.mc.player.isSleeping() && SomniaConfig.CLIENT.somniaGui.get() && !this.speedValues.isEmpty()) {
                double currentSpeed = this.speedValues.getLast();
                if (currentSpeed != 0) {
                    if (this.sleepStartTime == -1) this.sleepStartTime = this.mc.player.level.getGameTime();
                } else {
                    this.sleepStartTime = -1;
                }
                
                if (this.mc.screen != null && fatigue.getWakeTime() != -1 && currentSpeed != 0) {
                    renderSleepOverlay(poseStack, this.mc.screen, fatigue, currentSpeed);
                }
            }
        });
    }
    
    private void renderFatigueDisplay(PoseStack poseStack, double fatigue) {
        String str = SpeedColor.WHITE.color + (SomniaConfig.CLIENT.simpleFatigueDisplay.get()
            ? SideEffectStage.getSideEffectStageDescription(fatigue)
            : I18n.get("somnia.gui.fatigue", FATIGUE_FORMAT.format(fatigue)));
        int width = this.mc.font.width(str);
        int scaledWidth = this.mc.getWindow().getGuiScaledWidth();
        int scaledHeight = this.mc.getWindow().getGuiScaledHeight();
        FatigueDisplayPosition pos = this.mc.player.isSleeping() ? FatigueDisplayPosition.BOTTOM_RIGHT : SomniaConfig.CLIENT.fatigueDisplayPos.get();
        this.mc.font.draw(poseStack, str, pos.getX(scaledWidth, width), pos.getY(scaledHeight, this.mc.font.lineHeight), Integer.MIN_VALUE);
    }

    private void renderSleepOverlay(PoseStack poseStack, Screen screen, Fatigue fatigue, double currentSpeed) {
        long wakeTime = fatigue.getWakeTime();
        double sleepDuration = this.mc.level.getGameTime() - this.sleepStartTime;
        double remaining = wakeTime - this.sleepStartTime;
        double progress = sleepDuration / remaining;
        int width = screen.width - 40;

        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        renderProgressBar(screen, poseStack, width, progress);

        String displayETASleep = SomniaConfig.CLIENT.displayETASleep.get();
        int offsetX = displayETASleep.equals("center") ? screen.width / 2 - 80 : displayETASleep.equals("right") ? width - 160 : 0;
        renderScaledString(poseStack, offsetX + 20, String.format("%sx%s", SpeedColor.getColorForSpeed(currentSpeed).color, MULTIPLIER_FORMAT.format(currentSpeed)));

        double average = this.speedValues.stream()
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .summaryStatistics()
            .getAverage();
        long eta = Math.round((remaining - sleepDuration) / (average * 20));
        renderScaledString(poseStack, offsetX + 80, getETAString(eta));

        renderClock(poseStack, width);
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
        this.mc.font.drawShadow(poseStack, str, 0, 0, Integer.MIN_VALUE);
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