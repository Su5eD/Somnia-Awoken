package mods.su5ed.somnia.client.gui;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketPropUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;

public class ResetSpawnWidget extends Widget {
    private boolean resetSpawn = true;

    public ResetSpawnWidget(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent("Reset spawn: Yes"));
    }

    @Override
    public void onClick(double p_onClick_1_, double p_onClick_3_) {
        this.resetSpawn = !this.resetSpawn;
        this.setMessage(new StringTextComponent("Reset spawn: "+(resetSpawn ? "Yes" : "No")));
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).ifPresent(props -> {
            props.shouldResetSpawn(this.resetSpawn);
            Pair<PacketBuffer, Integer> packet = new PacketPropUpdate(0x01, 0x01, props.resetSpawn()).buildPacket();
            NetworkHandler.sendToServer(packet.getLeft(), packet.getRight());
        });
    }
}
