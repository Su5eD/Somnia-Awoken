package dev.su5ed.somnia.gui;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.Fatigue;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.server.ResetSpawnPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ResetSpawnButton extends AbstractButton {
    public boolean resetSpawn = true;

    public ResetSpawnButton(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, Component.translatable("somnia.gui.reset_spawn"));
    }

    @Override
    public void onPress() {
        this.resetSpawn = !this.resetSpawn;
        setMessage(Component.translatable(this.resetSpawn ? "somnia.gui.reset_spawn" : "somnia.gui.no_reset_spawn"));

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Fatigue fatigue = mc.player.getCapability(CapabilityFatigue.INSTANCE);
            if (fatigue != null) {
                SomniaNetwork.INSTANCE.sendToServer(new ResetSpawnPacket(this.resetSpawn));
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.HINT, getMessage());
    }
}
