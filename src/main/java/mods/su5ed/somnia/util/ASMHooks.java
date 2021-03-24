package mods.su5ed.somnia.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.handler.ServerTickHandler;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketUpdateWakeTime;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ASMHooks {
    public static boolean doMobSpawning(ServerWorld world) {
        boolean spawnMobs = world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        if (!SomniaConfig.disableCreatureSpawning || !spawnMobs) return spawnMobs;

        return ServerTickHandler.HANDLERS.stream()
                .filter(handler -> handler.worldServer == world)
                .map(handler -> handler.currentState != State.SIMULATING)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Couldn't find tick handler for given world"));
    }

    public static void updateWakeTime(PlayerEntity player) {
        player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
                .filter(props -> props.getWakeTime() < 0)
                .ifPresent(props -> {
                    long totalWorldTime = player.level.getGameTime();
                    long wakeTime = SomniaUtil.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
                    props.setWakeTime(wakeTime);
                    NetworkHandler.sendToClient(new PacketUpdateWakeTime(wakeTime), (ServerPlayerEntity) player);
                });
    }

    public static boolean skipRenderWorld(float partialTicks, long finishTimeNano, MatrixStack stack) {
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player.isSleeping() && SomniaConfig.disableRendering) {
                GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
                return true;
            }
            return false;
        });
    }
}
