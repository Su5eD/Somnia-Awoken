package dev.su5ed.somnia.capability;

import net.minecraft.nbt.CompoundTag;

public class FatigueStore implements Fatigue {
    private double fatigue;
    private double extraFatigueRate;
    private double replenishedFatigue;
    private int fatigueUpdateCounter;
    private int sideEffectStage = -1;
    private boolean resetSpawn = true;
    private boolean sleepOverride;
    private boolean sleepNormally;
    private long wakeTime = -1;

    @Override
    public double getFatigue() {
        return this.fatigue;
    }

    @Override
    public void setFatigue(double fatigue) {
        this.fatigue = fatigue;
    }

    @Override
    public int getSideEffectStage() {
        return this.sideEffectStage;
    }

    @Override
    public void setSideEffectStage(int stage) {
        this.sideEffectStage = stage;
    }

    @Override
    public boolean updateFatigueCounter() {
        return fatigueUpdateCounter++ % 100 == 0;
    }

    @Override
    public void maxFatigueCounter() {
        this.fatigueUpdateCounter = 100;
    }

    @Override
    public void setResetSpawn(boolean resetSpawn) {
        this.resetSpawn = resetSpawn;
    }

    @Override
    public boolean getResetSpawn() {
        return this.resetSpawn;
    }

    @Override
    public boolean sleepOverride() {
        return this.sleepOverride;
    }

    @Override
    public void setSleepOverride(boolean override) {
        this.sleepOverride = override;
    }

    @Override
    public void setSleepNormally(boolean sleepNormally) {
        this.sleepNormally = sleepNormally;
    }

    @Override
    public boolean shouldSleepNormally() {
        return this.sleepNormally;
    }

    @Override
    public long getWakeTime() {
        return this.wakeTime;
    }

    @Override
    public void setWakeTime(long wakeTime) {
        this.wakeTime = wakeTime;
    }

    @Override
    public double getExtraFatigueRate() {
        return this.extraFatigueRate;
    }

    @Override
    public void setExtraFatigueRate(double rate) {
        this.extraFatigueRate = rate;
    }

    @Override
    public double getReplenishedFatigue() {
        return this.replenishedFatigue;
    }

    @Override
    public void setReplenishedFatigue(double replenishedFatigue) {
        this.replenishedFatigue = replenishedFatigue;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("fatigue", this.fatigue);
        tag.putDouble("extraFatigueRate", this.extraFatigueRate);
        tag.putDouble("replenishedFatigue", this.replenishedFatigue);
        tag.putInt("sideEffectStage", this.sideEffectStage);
        tag.putBoolean("resetSpawn", this.resetSpawn);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.fatigue = nbt.getDouble("fatigue");
        this.extraFatigueRate = nbt.getDouble("extraFatigueRate");
        this.replenishedFatigue = nbt.getDouble("replenishedFatigue");
        this.sideEffectStage = nbt.getInt("sideEffectStage");
        this.resetSpawn = nbt.getBoolean("resetSpawn");
    }
}
