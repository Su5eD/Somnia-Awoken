package dev.su5ed.somnia.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.su5ed.somnia.api.capability.CapabilityFatigue;
import dev.su5ed.somnia.network.ActivateBlockPacket;
import dev.su5ed.somnia.network.SomniaNetwork;
import dev.su5ed.somnia.network.WakeTimeUpdatePacket;
import dev.su5ed.somnia.util.SomniaUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WakeTimeButton extends Button {
    private final String hoverText;
    private final String buttonText;

    public WakeTimeButton(int x, int y, int widthIn, int heightIn, String buttonText, long wakeTime) {
        super(x, y, widthIn, heightIn, new TextComponent(buttonText), button -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            long targetWakeTime = SomniaUtil.calculateWakeTime(mc.level.getGameTime(), (int) wakeTime);
            SomniaNetwork.INSTANCE.sendToServer(new WakeTimeUpdatePacket(targetWakeTime));
            mc.player.getCapability(CapabilityFatigue.INSTANCE)
                .ifPresent(props -> props.setWakeTime(targetWakeTime));

            HitResult mouseOver = mc.hitResult;
            if (mouseOver instanceof BlockHitResult blockHit) {
                Vec3 hitVec = mouseOver.getLocation();
                ActivateBlockPacket packet = new ActivateBlockPacket(blockHit.getBlockPos(), blockHit.getDirection(), (float) hitVec.x, (float) hitVec.y, (float) hitVec.z);
                SomniaNetwork.INSTANCE.sendToServer(packet);
            }

            mc.setScreen(null);
        });
        this.buttonText = buttonText;
        this.hoverText = SomniaUtil.timeStringForWorldTime(wakeTime);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        this.setMessage(new TextComponent(this.isHovered ? this.hoverText : this.buttonText));
    }
}
