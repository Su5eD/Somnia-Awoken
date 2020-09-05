package mods.su5ed.somnia.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.su5ed.somnia.Somnia;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class GuiSelectWakeTime extends Screen
{

	public GuiSelectWakeTime() {
		super(new StringTextComponent("Select Wake Time"));
	}

	@Override
	public void init() {
		int buttonWidth = 100, buttonHeight = 20;
		int buttonCenterX = this.width / 2 - 50;
		int buttonCenterY = this.height / 2 - 10;

		buttons.add(new ResetSpawnWidget(buttonCenterX, buttonCenterY - 22, buttonWidth, buttonHeight));
		buttons.add(new CancelWidget(buttonCenterX, buttonCenterY + 22, buttonWidth, buttonHeight));
		buttons.add(new HoverWidget(buttonCenterX, buttonCenterY + 88, buttonWidth, buttonHeight, "Midnight", 18000));
		buttons.add(new HoverWidget(buttonCenterX - 80, buttonCenterY + 66, buttonWidth, buttonHeight, "After Midnight", 20000));
		buttons.add(new HoverWidget(buttonCenterX - 110, buttonCenterY + 44, buttonWidth, buttonHeight, "Before Sunrise", 22000));
		buttons.add(new HoverWidget(buttonCenterX - 130, buttonCenterY + 22, buttonWidth, buttonHeight, "Mid Sunrise", 23000));
		buttons.add(new HoverWidget(buttonCenterX - 140, buttonCenterY, buttonWidth, buttonHeight, "After Sunrise", 0));
		buttons.add(new HoverWidget(buttonCenterX - 130, buttonCenterY - 22, buttonWidth, buttonHeight, "Early Morning", 1500));
		buttons.add(new HoverWidget(buttonCenterX - 110, buttonCenterY - 44, buttonWidth, buttonHeight, "Mid Morning", 3000));
		buttons.add(new HoverWidget(buttonCenterX - 80, buttonCenterY - 66, buttonWidth, buttonHeight, "Late Morning", 4500));
		buttons.add(new HoverWidget(buttonCenterX, buttonCenterY - 88, buttonWidth, buttonHeight, "Noon", 6000));
		buttons.add(new HoverWidget(buttonCenterX + 80, buttonCenterY - 66, buttonWidth, buttonHeight, "Early Afternoon", 7500));
		buttons.add(new HoverWidget(buttonCenterX + 110, buttonCenterY - 44, buttonWidth, buttonHeight, "Mid Afternoon", 9000));
		buttons.add(new HoverWidget(buttonCenterX + 130, buttonCenterY - 22, buttonWidth, buttonHeight, "Late Afternoon", 10500));
		buttons.add(new HoverWidget(buttonCenterX + 140, buttonCenterY, buttonWidth, buttonHeight, "Before Sunset", 12000));
		buttons.add(new HoverWidget(buttonCenterX + 130, buttonCenterY + 22, buttonWidth, buttonHeight, "Mid Sunset", 13000));
		buttons.add(new HoverWidget(buttonCenterX + 100, buttonCenterY + 44, buttonWidth, buttonHeight, "After Sunset", 14000));
		buttons.add(new HoverWidget(buttonCenterX + 88, buttonCenterY + 66, buttonWidth, buttonHeight, "Before Midnight", 16000));
	}


	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		drawCenteredString(matrixStack, this.font, "Sleep until...?", this.width / 2, this.height / 2 - 5, 16777215);
		if (this.minecraft != null && this.minecraft.player != null) drawCenteredString(matrixStack, this.font, Somnia.timeStringForWorldTime(this.minecraft.player.world.getDayTime()), this.width/2, this.height/2 - 66, 16777215);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}