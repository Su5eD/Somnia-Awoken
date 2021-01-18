package mods.su5ed.somnia.api.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityFatigueProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundNBT> {
    private final Fatigue instance = new Fatigue();

    @Override
    public CompoundNBT serializeNBT()
    {
        return this.instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.instance.deserializeNBT(nbt);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFatigue.FATIGUE_CAPABILITY) return LazyOptional.of(() -> (T) this.instance);
        return LazyOptional.empty();
    }
}
