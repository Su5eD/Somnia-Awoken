package mods.su5ed.somnia.common.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.client.SomniaClient;
import mods.su5ed.somnia.common.config.SomniaConfig;
import mods.su5ed.somnia.server.ServerTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ASMHooks {
    public static void tick() {
        Somnia.instance.tickHandlers.forEach(ServerTickHandler::tickStart);
    }

    public static boolean doMobSpawning(ServerWorld world) {
        boolean spawnMobs = world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
        if (!SomniaConfig.disableCreatureSpawning || !spawnMobs) return spawnMobs;

        return Somnia.instance.tickHandlers.stream()
                .filter(handler -> handler.worldServer == world)
                .map(handler -> handler.currentState != SomniaState.SIMULATING)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("tickHandlers doesn't contain match for given world server"));
    }

    public static void updateWakeTime(PlayerEntity player) {
        if (SomniaClient.autoWakeTime < 0) {
            long totalWorldTime = player.world.getGameTime();
            SomniaClient.autoWakeTime = SomniaUtil.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
        }
    }

    public static void renderWorld(float partialTicks, long finishTimeNano, MatrixStack stack) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player.isSleeping() && SomniaConfig.disableRendering) GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
            else mc.gameRenderer.renderWorld(partialTicks, finishTimeNano, stack);
        });
    }
}
