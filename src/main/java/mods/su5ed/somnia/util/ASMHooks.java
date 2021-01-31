package mods.su5ed.somnia.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.handler.ServerTickHandler;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketUpdateWakeTime;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ASMHooks {
    public static boolean doMobSpawning(ServerWorld world) {
        boolean spawnMobs = world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
        if (!SomniaConfig.disableCreatureSpawning || !spawnMobs) return spawnMobs;

        return ServerTickHandler.HANDLERS.stream()
                .filter(handler -> handler.worldServer == world)
                .map(handler -> handler.currentState != State.SIMULATING)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Couldn't find tick handler for given world"));
    }

    public static void updateWakeTime(PlayerEntity player) {
        player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
                .map(IFatigue::getWakeTime)
                .filter(wakeTime -> wakeTime < 0)
                .ifPresent(wakeTime -> {
                    long totalWorldTime = player.world.getGameTime();
                    wakeTime = SomniaUtil.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
                    NetworkHandler.INSTANCE.sendToServer(new PacketUpdateWakeTime(wakeTime));
                });
    }

    public static void renderWorld(float partialTicks, long finishTimeNano, MatrixStack stack) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player.isSleeping() && SomniaConfig.disableRendering) GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
            else mc.gameRenderer.renderWorld(partialTicks, finishTimeNano, stack);
        });
    }
}
