package com.kingrunes.somnia.common.capability;

import net.minecraft.nbt.NBTTagCompound;

//Thanks @TheSilkMiner for a custom capability example
public class Fatigue implements IFatigue {

    private double fatigue;
    private int fatigueUpdateCounter = 0, sideEffectStage = -1;

    @Override
    public double getFatigue()
    {
        return this.fatigue;
    }

    @Override
    public void setFatigue(double fatigue)
    {
        this.fatigue = fatigue;
    }

    @Override
    public int getSideEffectStage()
    {
        return this.sideEffectStage;
    }

    @Override
    public void setSideEffectStage(int stage)
    {
        this.sideEffectStage = stage;
    }

    @Override
    public int updateFatigueCounter()
    {
        return ++fatigueUpdateCounter;
    }

    @Override
    public void resetFatigueCounter()
    {
        this.fatigueUpdateCounter = 0;
    }


    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("fatigue", this.fatigue);
        tag.setInteger("sideEffectStage", this.sideEffectStage);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.fatigue = nbt.getDouble("fatigue");
        this.sideEffectStage = nbt.getInteger("sideEffectStage");
    }
}
