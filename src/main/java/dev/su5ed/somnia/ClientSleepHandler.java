package dev.su5ed.somnia;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.Fatigue;
import dev.su5ed.somnia.util.FatigueDisplayPosition;
import dev.su5ed.somnia.util.ScreenPosition;
import dev.su5ed.somnia.util.SideEffectStage;
import dev.su5ed.somnia.util.SpeedColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
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
    private double previousVolume;

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

                OptionInstance<Double> option = this.mc.options.getSoundSourceOptionInstance(SoundSource.MASTER);
                this.previousVolume = option.get();
                option.set(0.0);
            }
            else if (this.muted) {
                this.muted = false;
                this.mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(this.previousVolume);
            }
        }
    }

    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        this.sleepStartTime = -1;
        this.speedValues.clear();
    }

    @SubscribeEvent
    public void renderGui(RenderGuiEvent.Post event) {
        if (this.mc.screen != null && !(this.mc.screen instanceof PauseScreen) && (this.mc.player == null || !this.mc.player.isSleeping())) return;

        this.mc.player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(fatigue -> {
            GuiGraphics guigraphics = event.getGuiGraphics();
            double fatigueAmount = fatigue.getFatigue();
            if (SomniaConfig.COMMON.enableFatigue.get() && !this.mc.player.isCreative() && !this.mc.player.isSpectator() && !this.mc.options.hideGui
                && (this.mc.player.isSleeping() || SomniaConfig.COMMON.fatigueSideEffects.get() || fatigueAmount <= SomniaConfig.COMMON.minimumFatigueToSleep.get())
            ) {
                renderFatigueDisplay(guigraphics, fatigueAmount);
            }
        });
    }

    @SubscribeEvent
    public void screenRenderPost(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof InBedChatScreen && SomniaConfig.CLIENT.somniaGui.get() && !this.speedValues.isEmpty()) {
            this.mc.player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(fatigue -> {
                double currentSpeed = this.speedValues.getLast();
                if (currentSpeed != 0) {
                    if (this.sleepStartTime == -1) this.sleepStartTime = this.mc.player.level().getGameTime();
                }
                else {
                    this.sleepStartTime = -1;
                }

                if (this.mc.screen != null && fatigue.getWakeTime() != -1 && currentSpeed != 0) {
                    renderSleepOverlay(event.getGuiGraphics(), this.mc.screen, fatigue, currentSpeed);
                }
            });
        }
    }

    private void renderFatigueDisplay(GuiGraphics guigraphics, double fatigue) {
        FatigueDisplayPosition pos = this.mc.player.isSleeping() ? FatigueDisplayPosition.BOTTOM_RIGHT : SomniaConfig.CLIENT.fatigueDisplayPos.get();
        if (pos != FatigueDisplayPosition.NONE) {
            String str = SpeedColor.WHITE.color + (SomniaConfig.CLIENT.simpleFatigueDisplay.get()
                ? SideEffectStage.getSideEffectStageDescription(fatigue)
                : I18n.get("somnia.gui.fatigue", FATIGUE_FORMAT.format(fatigue)));
            int width = this.mc.font.width(str);
            int scaledWidth = this.mc.getWindow().getGuiScaledWidth();
            int scaledHeight = this.mc.getWindow().getGuiScaledHeight();
            guigraphics.drawString(this.mc.font, str, pos.getX(scaledWidth, width), pos.getY(scaledHeight, this.mc.font.lineHeight), Integer.MIN_VALUE, false);
        }
    }

    private void renderSleepOverlay(GuiGraphics guiGraphics, Screen screen, Fatigue fatigue, double currentSpeed) {
        long wakeTime = fatigue.getWakeTime();
        double sleepDuration = this.mc.level.getGameTime() - this.sleepStartTime;
        double remaining = wakeTime - this.sleepStartTime;
        double progress = sleepDuration / remaining;
        int width = screen.width - 40;

        renderProgressBar(guiGraphics, width, progress);

        ScreenPosition displayETASleep = SomniaConfig.CLIENT.displayETASleep.get();
        if (displayETASleep != ScreenPosition.NONE) {
            int offsetX = displayETASleep == ScreenPosition.CENTER ? screen.width / 2 - 80 : displayETASleep == ScreenPosition.RIGHT ? width - 160 : 0;
            renderScaledString(guiGraphics, offsetX + 20, String.format("%sx%s", SpeedColor.getColorForSpeed(currentSpeed).color, MULTIPLIER_FORMAT.format(currentSpeed)));

            double average = this.speedValues.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics()
                .getAverage();
            long eta = Math.round((remaining - sleepDuration) / (average * 20));
            renderScaledString(guiGraphics, offsetX + 80, getETAString(eta));
        }

        renderClock(guiGraphics, width);
    }

    private String getETAString(long totalSeconds) {
        long etaSeconds = totalSeconds % 60;
        long etaMinutes = (totalSeconds - etaSeconds) / 60;
        return String.format(SpeedColor.WHITE.color + "(%s:%s)", (etaMinutes < 10 ? "0" : "") + etaMinutes, (etaSeconds < 10 ? "0" : "") + etaSeconds);
    }

    private void renderProgressBar(GuiGraphics poseStack, int width, double progress) {
        doRenderProgressBar(poseStack, width, 1, 0.2F);
        doRenderProgressBar(poseStack, width, progress, 1.0F);
    }

    private void doRenderProgressBar(GuiGraphics guiGraphics, int width, double progress, float alpha) {
        for (int amount = (int) (progress * width), x = 20; amount > 0; amount -= 180, x += 180) {
            blit(guiGraphics, Gui.GUI_ICONS_LOCATION, x, 10, 0, 69, Math.min(amount, 180), 5, 1, 1, 1, alpha);
        }
    }

    private void renderScaledString(GuiGraphics guiGraphics, int x, String str) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, 20, 0);
        poseStack.scale(1.5F, 1.5F, 1);
        guiGraphics.drawString(this.mc.font, str, 0, 0, Integer.MIN_VALUE);
        poseStack.popPose();
    }

    private void renderClock(GuiGraphics guiGraphics, int width) {
        int x = switch (SomniaConfig.CLIENT.somniaGuiClockPosition.get()) {
            case LEFT -> 40;
            case CENTER -> width / 2;
            case RIGHT -> width - 40;
            default -> -1;
        };
        if (x != -1) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(x, 35, 0);
            poseStack.scale(4, 4, 1);
            guiGraphics.renderItem(CLOCK, 0, 0);
            poseStack.popPose();
        }
    }

    private static void blit(GuiGraphics guiGraphics, ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight, float red, float green, float blue, float alpha) {
        guiGraphics.innerBlit(atlasLocation, x, x + uWidth, y, y + vHeight, 0, (uOffset + 0.0F) / 256F, (uOffset + (float) uWidth) / 256F, (vOffset + 0.0F) / 256F, (vOffset + (float) vHeight) / 256F, red, green, blue, alpha);
    }
}