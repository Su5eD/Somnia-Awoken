package mods.su5ed.somnia.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class CancelButton extends Button {

    public CancelButton(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent("Cancel"), button -> Minecraft.getInstance().setScreen(null));
    }
}
