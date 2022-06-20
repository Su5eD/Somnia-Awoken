package dev.su5ed.somnia.api.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CapabilityFatigueStorage implements Capability.IStorage<IFatigue> {

    @Nullable
    @Override
    public INBT writeNBT(Capability<IFatigue> capability, IFatigue instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IFatigue> capability, IFatigue instance, Direction side, INBT nbt) {
        if (!(nbt instanceof CompoundNBT)) throw new IllegalArgumentException("An NBTTagCompound is required");
        instance.deserializeNBT((CompoundNBT) nbt);
    }
}
