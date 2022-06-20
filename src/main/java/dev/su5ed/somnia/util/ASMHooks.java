package dev.su5ed.somnia.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.core.SomniaConfig;
import dev.su5ed.somnia.handler.ServerTickHandler;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.WakeTimeUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ASMHooks {
    public static boolean doMobSpawning(ServerLevel level) {
        boolean spawnMobs = level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        if (!SomniaConfig.COMMON.disableCreatureSpawning.get() || !spawnMobs) return spawnMobs;

        return ServerTickHandler.HANDLERS.stream()
            .filter(handler -> handler.serverLevel == level)
            .map(handler -> handler.currentState != State.SIMULATING)
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Couldn't find tick handler for given world"));
    }

    public static void updateWakeTime(Player player) {
        player.getCapability(CapabilityFatigue.INSTANCE)
            .filter(props -> props.getWakeTime() < 0)
            .ifPresent(props -> {
                long totalWorldTime = player.level.getGameTime();
                long wakeTime = SomniaUtil.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
                props.setWakeTime(wakeTime);
                SomniaNetwork.sendToClient(new WakeTimeUpdatePacket(wakeTime), (ServerPlayer) player);
            });
    }

    public static boolean skipRenderWorld(float partialTicks, long finishTimeNano, PoseStack stack) {
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player.isSleeping() && SomniaConfig.CLIENT.disableRendering.get()) {
                GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
                return true;
            }
            return false;
        });
    }
}
