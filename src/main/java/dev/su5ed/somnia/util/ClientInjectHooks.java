package dev.su5ed.somnia.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.su5ed.somnia.SomniaConfig;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public final class ClientInjectHooks {

    public static boolean skipRenderWorld(float partialTicks, long finishTimeNano, PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player.isSleeping() && SomniaConfig.CLIENT.disableRendering.get()) {
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            return true;
        }
        return false;
    }

    private ClientInjectHooks() {}
}
