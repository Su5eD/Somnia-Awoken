package mods.su5ed.somnia.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.client.SomniaClient;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.server.ServerTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ASMHooks {
    public static void tick() {
        synchronized (Somnia.instance.tickHandlers) {
            for (ServerTickHandler serverTickHandler : Somnia.instance.tickHandlers) serverTickHandler.tickStart();
        }
    }

    public static boolean doMobSpawning(ServerWorld par1WorldServer) {
        boolean defValue = par1WorldServer.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
        if (!SomniaConfig.disableCreatureSpawning || !defValue) return defValue;

        for (ServerTickHandler serverTickHandler : Somnia.instance.tickHandlers) {
            if (serverTickHandler.worldServer == par1WorldServer) return serverTickHandler.currentState != SomniaState.ACTIVE;
        }

        throw new IllegalStateException("tickHandlers doesn't contain match for given world server");
    }

    public static void updateWakeTime(PlayerEntity player) {
        if (SomniaClient.autoWakeTime != -1) return; //Don't change the wake time if it's already been selected
        long totalWorldTime = player.world.getGameTime();
        SomniaClient.autoWakeTime = SomniaUtil.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderWorld(float partialTicks, long finishTimeNano, MatrixStack matrixStackIn) {
        if (Minecraft.getInstance().player.isSleeping() && SomniaConfig.disableRendering) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        } else Minecraft.getInstance().gameRenderer.renderWorld(partialTicks, finishTimeNano, matrixStackIn);
    }
}
