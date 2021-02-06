package mods.su5ed.somnia.network.packet.handler;

import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.gui.WakeTimeSelectScreen;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {
    public static void updateWakeTime(long wakeTime) {
        Minecraft.getInstance().player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> props.setWakeTime(wakeTime));
    }

    public static void openGUI() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.currentScreen instanceof WakeTimeSelectScreen)) mc.displayGuiScreen(new WakeTimeSelectScreen());
    }
}
