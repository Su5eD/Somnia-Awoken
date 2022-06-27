package mods.su5ed.somnia.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketActivateBlock;
import mods.su5ed.somnia.network.packet.PacketUpdateWakeTime;
import mods.su5ed.somnia.util.SomniaUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;

public class WakeTimeButton extends Button {
    private final String hoverText;
    private final String buttonText;

    public WakeTimeButton(int x, int y, int widthIn, int heightIn, String buttonText, long wakeTime) {
        super(x, y, widthIn, heightIn, new StringTextComponent(buttonText), button -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            long targetWakeTime = SomniaUtil.calculateWakeTime(mc.level, (int) wakeTime);
            NetworkHandler.INSTANCE.sendToServer(new PacketUpdateWakeTime(targetWakeTime));
            mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
                    .ifPresent(props -> props.setWakeTime(targetWakeTime));

            RayTraceResult mouseOver = mc.hitResult;
            if (mouseOver instanceof BlockRayTraceResult) {
                Vector3d hitVec = mouseOver.getLocation();
                PacketActivateBlock packet = new PacketActivateBlock(((BlockRayTraceResult)mouseOver).getBlockPos(), ((BlockRayTraceResult)mouseOver).getDirection(), (float) hitVec.x, (float) hitVec.y, (float) hitVec.z);
                NetworkHandler.INSTANCE.sendToServer(packet);
            }

            mc.setScreen(null);
        });
        this.buttonText = buttonText;
        this.hoverText = SomniaUtil.timeStringForWorldTime(wakeTime);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        this.setMessage(new StringTextComponent(this.isHovered ? this.hoverText : this.buttonText));
    }
}
