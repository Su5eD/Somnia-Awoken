package mods.su5ed.somnia.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

public class CancelWidget extends Widget {

    public CancelWidget(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent("Cancel"));
    }

    @Override
    public void onClick(double p_onClick_1_, double p_onClick_3_) {
        Minecraft.getInstance().displayGuiScreen(null);
    }
}
