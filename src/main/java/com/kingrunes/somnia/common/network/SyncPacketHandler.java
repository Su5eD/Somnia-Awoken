package com.kingrunes.somnia.common.network;

import com.kingrunes.somnia.common.capability.CapabilityFatigue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.Callable;

public class SyncPacketHandler implements IMessageHandler<SyncPacket, IMessage> {
    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(SyncPacket message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                Minecraft.getMinecraft().player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).deserializeNBT(message.capabilityNBT);
            }
        });
        return null;
    }
}
