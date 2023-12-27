package dev.su5ed.somnia.network;

import dev.su5ed.somnia.capability.CapabilityFatigue;
import dev.su5ed.somnia.capability.Fatigue;
import dev.su5ed.somnia.gui.WakeTimeSelectScreen;
import net.minecraft.client.Minecraft;

public final class ClientPacketHandler {

    public static void updateWakeTime(long wakeTime) {
        Fatigue fatigue = Minecraft.getInstance().player.getCapability(CapabilityFatigue.INSTANCE);
        if (fatigue != null) {
            fatigue.setWakeTime(wakeTime);
        }
    }

    public static void openGUI() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof WakeTimeSelectScreen)) mc.setScreen(new WakeTimeSelectScreen());
    }

    public static void wakeUpPlayer() {
        Minecraft.getInstance().player.stopSleeping();
    }

    public static void updateFatigue(double fatigue) {
        Fatigue props = Minecraft.getInstance().player.getCapability(CapabilityFatigue.INSTANCE);
        if (props != null) {
            props.setFatigue(fatigue);
        }
    }

    private ClientPacketHandler() {}
}
