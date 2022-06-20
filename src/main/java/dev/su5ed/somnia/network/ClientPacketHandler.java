package dev.su5ed.somnia.network;

import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.gui.WakeTimeSelectScreen;
import net.minecraft.client.Minecraft;

public final class ClientPacketHandler {
    public static void updateWakeTime(long wakeTime) {
        Minecraft.getInstance().player.getCapability(CapabilityFatigue.INSTANCE).ifPresent(props -> props.setWakeTime(wakeTime));
    }

    public static void openGUI() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof WakeTimeSelectScreen)) mc.setScreen(new WakeTimeSelectScreen());
    }
    
    private ClientPacketHandler() {}
}
