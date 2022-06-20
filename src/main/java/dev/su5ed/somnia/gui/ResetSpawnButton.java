package dev.su5ed.somnia.gui;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.ResetSpawnPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public class ResetSpawnButton extends Button {
    public boolean resetSpawn = true;

    public ResetSpawnButton(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new TextComponent("Reset spawn: Yes"), button -> { // TODO Localization
            ((ResetSpawnButton) button).resetSpawn = !((ResetSpawnButton) button).resetSpawn;
            button.setMessage(new TextComponent("Reset spawn: "+(((ResetSpawnButton) button).resetSpawn ? "Yes" : "No")));
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> {
                    props.shouldResetSpawn(((ResetSpawnButton) button).resetSpawn);
                    SomniaNetwork.INSTANCE.sendToServer(new ResetSpawnPacket(props.resetSpawn()));
                });
            }
        });
    }
}
