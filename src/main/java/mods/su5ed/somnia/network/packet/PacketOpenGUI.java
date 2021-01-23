package mods.su5ed.somnia.network.packet;

import mods.su5ed.somnia.gui.WakeTimeSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenGUI {
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (!(mc.currentScreen instanceof WakeTimeSelectScreen)) mc.displayGuiScreen(new WakeTimeSelectScreen());
        }));
        return true;
    }
}
