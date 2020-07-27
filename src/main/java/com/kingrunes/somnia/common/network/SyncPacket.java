package com.kingrunes.somnia.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class SyncPacket implements IMessage {

    public SyncPacket(){}

    public NBTTagCompound capabilityNBT;

    public SyncPacket(NBTTagCompound capabilityNBT) {
        this.capabilityNBT = capabilityNBT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.capabilityNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.capabilityNBT);
    }
}
