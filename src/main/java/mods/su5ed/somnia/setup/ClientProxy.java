package mods.su5ed.somnia.setup;

import mods.su5ed.somnia.client.ClientTickHandler;
import mods.su5ed.somnia.client.gui.GuiSelectWakeTime;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientProxy implements IProxy {
    public static double playerFatigue = -1;
    public static final ClientTickHandler clientTickHandler = new ClientTickHandler();

    @Override
    public void register()
    {
        MinecraftForge.EVENT_BUS.register(clientTickHandler);
    }

    @Override
    public void handleGUIOpenPacket() {
        Minecraft mc = Minecraft.getInstance();
        mc.displayGuiScreen(new GuiSelectWakeTime());
    }

    @Override
    public void handlePropUpdatePacket(DataInputStream in) throws IOException {
        byte target = in.readByte();
        Minecraft mc = Minecraft.getInstance();

        switch (target)
        {
            case 0x00:
                if (mc.player != null && mc.player.isSleeping())
                {
                    int b = in.readInt();
                    for (int a=0; a<b; a++)
                        clientTickHandler.readField(in);
                }
                break;
            case 0x01:
                int b = in.readInt();
                for (int a=0; a<b; a++)
                {
                    if (in.readByte() == 0x00) {
                        playerFatigue = in.readDouble();
                    }
                }
                break;
        }
    }

    @Override
    public void handleWakePacket(ServerPlayerEntity player) {
        Minecraft.getInstance().displayGuiScreen(null);
        player.wakeUp();
    }
}
