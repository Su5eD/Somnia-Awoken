package dev.su5ed.somnia.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.su5ed.somnia.util.SomniaUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class WakeTimeSelectScreen extends Screen {

    public WakeTimeSelectScreen() {
        super(new TextComponent("Select Wake Time"));
    }

    @Override
    public void init() {
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonCenterX = this.width / 2 - 50;
        int buttonCenterY = this.height / 2 - 10;

        addRenderableWidget(new ResetSpawnButton(buttonCenterX, buttonCenterY - 22, buttonWidth, buttonHeight));
        addRenderableWidget(new CancelButton(buttonCenterX, buttonCenterY + 22, buttonWidth, buttonHeight));
        addRenderableWidget(new WakeTimeButton(buttonCenterX, buttonCenterY + 88, buttonWidth, buttonHeight, "Midnight", 18000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 80, buttonCenterY + 66, buttonWidth, buttonHeight, "After Midnight", 20000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 110, buttonCenterY + 44, buttonWidth, buttonHeight, "Before Sunrise", 22000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 130, buttonCenterY + 22, buttonWidth, buttonHeight, "Mid Sunrise", 23000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 140, buttonCenterY, buttonWidth, buttonHeight, "After Sunrise", 0));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 130, buttonCenterY - 22, buttonWidth, buttonHeight, "Early Morning", 1500));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 110, buttonCenterY - 44, buttonWidth, buttonHeight, "Mid Morning", 3000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 80, buttonCenterY - 66, buttonWidth, buttonHeight, "Late Morning", 4500));
        addRenderableWidget(new WakeTimeButton(buttonCenterX, buttonCenterY - 88, buttonWidth, buttonHeight, "Noon", 6000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 80, buttonCenterY - 66, buttonWidth, buttonHeight, "Early Afternoon", 7500));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 110, buttonCenterY - 44, buttonWidth, buttonHeight, "Mid Afternoon", 9000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 130, buttonCenterY - 22, buttonWidth, buttonHeight, "Late Afternoon", 10500));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 140, buttonCenterY, buttonWidth, buttonHeight, "Before Sunset", 12000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 130, buttonCenterY + 22, buttonWidth, buttonHeight, "Mid Sunset", 13000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 100, buttonCenterY + 44, buttonWidth, buttonHeight, "After Sunset", 14000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 88, buttonCenterY + 66, buttonWidth, buttonHeight, "Before Midnight", 16000));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        
        drawCenteredString(poseStack, this.font, "Sleep until...?", this.width / 2, this.height / 2 - 5, 16777215);
        if (this.minecraft != null && this.minecraft.player != null) {
            String time = SomniaUtil.timeStringForGameTime(this.minecraft.player.level.getDayTime());
            drawCenteredString(poseStack, this.font, time, this.width / 2, this.height / 2 - 66, 16777215);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}