package com.kingrunes.somnia.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class FatigueCapabilityStorage implements Capability.IStorage<IFatigue> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IFatigue> capability, IFatigue instance, EnumFacing side)
    {
        if (instance == null) throw new IllegalArgumentException("Fatigue can't be null");
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IFatigue> capability, IFatigue instance, EnumFacing side, NBTBase nbt)
    {
        if (instance == null) throw new IllegalArgumentException("Fatigue can't be null");
        if (!(nbt instanceof NBTTagCompound)) throw new IllegalArgumentException("An NBTTagCompound is required");
        instance.deserializeNBT((NBTTagCompound) nbt);
    }
}
