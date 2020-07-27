package com.kingrunes.somnia.common.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IFatigue extends INBTSerializable<NBTTagCompound> {
    double getFatigue();

    void setFatigue(double fatigue);

    int getSideEffectStage();

    void setSideEffectStage(int stage);

    int updateFatigueCounter();

    void resetFatigueCounter();
}
