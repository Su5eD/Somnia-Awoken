package mods.su5ed.somnia.setup;

import mods.su5ed.somnia.common.config.SomniaConfig;
import mods.su5ed.somnia.common.util.TimePeriod;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.io.DataInputStream;

public class ServerProxy implements IProxy {
    public static TimePeriod enterSleepPeriod = new TimePeriod(SomniaConfig.enterSleepStart, SomniaConfig.enterSleepEnd);
    public static TimePeriod validSleepPeriod = new TimePeriod(SomniaConfig.validSleepStart, SomniaConfig.validSleepEnd);


    @Override
    public void register() {}


    @Override
    public void handleGUIOpenPacket() {}

    @Override
    public void handlePropUpdatePacket(DataInputStream in) {}

    @Override
    public void handleWakePacket(ServerPlayerEntity player) {
        player.wakeUp();
    }
}
