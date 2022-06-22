package dev.su5ed.somnia.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

// Source:
// https://github.com/VazkiiMods/Patchouli/blob/e3865d8c567b043296cdc4e6964a35c1ae4978cf/Xplat/src/main/java/vazkii/patchouli/client/RenderHelper.java
public final class RenderHelper {
    public static void renderItemStackInGui(PoseStack ms, ItemStack stack, int x, int y) {
        transferMsToGl(ms, () -> Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y));
    }
        
    public static void transferMsToGl(PoseStack ms, Runnable toRun) {
        PoseStack mvs = RenderSystem.getModelViewStack();
        mvs.pushPose();
        mvs.mulPoseMatrix(ms.last().pose());
        RenderSystem.applyModelViewMatrix();
        toRun.run();
        mvs.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private RenderHelper() {}
}
