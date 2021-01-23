package mods.su5ed.somnia.core;

import mods.su5ed.somnia.handler.ClientTickHandler;

public class SomniaClient {
    public static final ClientTickHandler clientTickHandler = new ClientTickHandler();
    public static long autoWakeTime = -1; //TODO: Wake players from server side
}
