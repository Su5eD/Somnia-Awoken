package com.kingrunes.somnia.common.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FatigueCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<NBTTagCompound> {
    private final Fatigue instance = new Fatigue();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityFatigue.FATIGUE_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityFatigue.FATIGUE_CAPABILITY) return CapabilityFatigue.FATIGUE_CAPABILITY.cast(this.instance);
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return this.instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.instance.deserializeNBT(nbt);
    }
}
