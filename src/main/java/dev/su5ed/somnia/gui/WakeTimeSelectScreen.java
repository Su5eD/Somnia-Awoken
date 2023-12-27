package dev.su5ed.somnia.gui;

import dev.su5ed.somnia.util.SomniaUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WakeTimeSelectScreen extends Screen {

    public WakeTimeSelectScreen() {
        super(Component.translatable("somnia.gui.select_wake_time"));
    }

    @Override
    public void init() {
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonCenterX = this.width / 2 - 50;
        int buttonCenterY = this.height / 2 - 10;

        addRenderableWidget(new ResetSpawnButton(buttonCenterX, buttonCenterY - 22, buttonWidth, buttonHeight));
        addRenderableWidget(new CancelButton(buttonCenterX, buttonCenterY + 22, buttonWidth, buttonHeight));

        addWakeTimeButton(buttonCenterX, buttonCenterY + 88, buttonWidth, buttonHeight, "midnight", 18000);
        addWakeTimeButton(buttonCenterX - 80, buttonCenterY + 66, buttonWidth, buttonHeight, "after_midnight", 20000);
        addWakeTimeButton(buttonCenterX - 110, buttonCenterY + 44, buttonWidth, buttonHeight, "before_sunrise", 22000);
        addWakeTimeButton(buttonCenterX - 130, buttonCenterY + 22, buttonWidth, buttonHeight, "mid_sunrise", 23000);
        addWakeTimeButton(buttonCenterX - 140, buttonCenterY, buttonWidth, buttonHeight, "after_sunrise", 0);
        addWakeTimeButton(buttonCenterX - 130, buttonCenterY - 22, buttonWidth, buttonHeight, "early_morning", 1500);
        addWakeTimeButton(buttonCenterX - 110, buttonCenterY - 44, buttonWidth, buttonHeight, "mid_morning", 3000);
        addWakeTimeButton(buttonCenterX - 80, buttonCenterY - 66, buttonWidth, buttonHeight, "late_morning", 4500);
        addWakeTimeButton(buttonCenterX, buttonCenterY - 88, buttonWidth, buttonHeight, "noon", 6000);
        addWakeTimeButton(buttonCenterX + 80, buttonCenterY - 66, buttonWidth, buttonHeight, "early_afternoon", 7500);
        addWakeTimeButton(buttonCenterX + 110, buttonCenterY - 44, buttonWidth, buttonHeight, "mid_afternoon", 9000);
        addWakeTimeButton(buttonCenterX + 130, buttonCenterY - 22, buttonWidth, buttonHeight, "late_afternoon", 1050);
        addWakeTimeButton(buttonCenterX + 140, buttonCenterY, buttonWidth, buttonHeight, "before_sunset", 12000);
        addWakeTimeButton(buttonCenterX + 130, buttonCenterY + 22, buttonWidth, buttonHeight, "mid_sunset", 13000);
        addWakeTimeButton(buttonCenterX + 100, buttonCenterY + 44, buttonWidth, buttonHeight, "after_sunset", 14000);
        addWakeTimeButton(buttonCenterX + 88, buttonCenterY + 66, buttonWidth, buttonHeight, "before_midnight", 16000);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, Component.translatable("somnia.gui.wts_title"), this.width / 2, this.height / 2 - 5, 16777215);
        if (this.minecraft != null && this.minecraft.player != null) {
            String time = SomniaUtil.timeStringForGameTime(SomniaUtil.getLevelDayTime(this.minecraft.level));
            guiGraphics.drawCenteredString(this.font, time, this.width / 2, this.height / 2 - 66, 16777215);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void addWakeTimeButton(int x, int y, int width, int height, String translationKey, int wakeTime) {
        addRenderableWidget(new WakeTimeButton(x, y, width, height, Component.translatable("somnia.gui.time_" + translationKey), wakeTime));
    }
}