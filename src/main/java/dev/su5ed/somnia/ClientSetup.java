package dev.su5ed.somnia;

import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;

public final class ClientSetup {

    static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("fatigue_overlay", ClientSleepHandler.INSTANCE::renderGuiOverlay);
    }
    
    private ClientSetup() {}
}
