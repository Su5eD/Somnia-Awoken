package mods.su5ed.somnia.gui;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketResetSpawn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class ResetSpawnButton extends Button {
    public boolean resetSpawn = true;

    public ResetSpawnButton(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent("Reset spawn: Yes"), button -> {
            ((ResetSpawnButton) button).resetSpawn = !((ResetSpawnButton) button).resetSpawn;
            button.setMessage(new StringTextComponent("Reset spawn: "+(((ResetSpawnButton) button).resetSpawn ? "Yes" : "No")));
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(props -> {
                props.shouldResetSpawn(((ResetSpawnButton) button).resetSpawn);
                NetworkHandler.INSTANCE.sendToServer(new PacketResetSpawn(props.resetSpawn()));
            });
        });
    }
}
