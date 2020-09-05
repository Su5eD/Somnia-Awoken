package mods.su5ed.somnia.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.su5ed.somnia.Somnia;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketActivateBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;

public class HoverWidget extends Widget {
    private final long wakeTime;
    private final String hoverText;
    private final String buttonText;

    public HoverWidget(int x, int y, int widthIn, int heightIn, String buttonText, long wakeTime) {
        super(x, y, widthIn, heightIn, new StringTextComponent(buttonText));
        this.wakeTime = wakeTime;
        this.buttonText = buttonText;
        this.hoverText = Somnia.timeStringForWorldTime(wakeTime);
    }

    @Override
    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrix, mouseX, mouseY, partialTicks);
        this.setMessage(new StringTextComponent(this.isHovered ? this.hoverText : this.buttonText));
    }

    @Override
    public void onClick(double p_onClick_1_, double p_onClick_3_) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null) return;

        Somnia.clientAutoWakeTime = Somnia.calculateWakeTime(mc.world.getGameTime(), (int) this.wakeTime);
        /*
         * Nice little hack to simulate a right click on the bed, don't try this at home kids
         */
		RayTraceResult mouseOver = mc.objectMouseOver;

		if (mouseOver instanceof BlockRayTraceResult) {
            Vector3d hitVec = mouseOver.getHitVec();
			Pair<PacketBuffer, Integer> packet = new PacketActivateBlock(((BlockRayTraceResult)mouseOver).getPos(), ((BlockRayTraceResult)mouseOver).getFace(), (float) hitVec.x, (float) hitVec.y, (float) hitVec.z).buildPacket();
		    NetworkHandler.sendToServer(packet.getLeft(), packet.getRight());
		}

		mc.displayGuiScreen(null);
    }
}
