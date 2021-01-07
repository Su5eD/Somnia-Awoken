package mods.su5ed.somnia.client;

import net.minecraftforge.common.MinecraftForge;

public class SomniaClient {
    public static double playerFatigue = -1;
    public static final ClientTickHandler clientTickHandler = new ClientTickHandler();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(clientTickHandler);
    }
}
