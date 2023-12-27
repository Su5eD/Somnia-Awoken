package dev.su5ed.somnia.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FatigueStore implements Fatigue {
    public static final Codec<FatigueStore> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("fatigue").forGetter(FatigueStore::getFatigue),
        Codec.DOUBLE.fieldOf("extraFatigueRate").forGetter(FatigueStore::getExtraFatigueRate),
        Codec.DOUBLE.fieldOf("replenishedFatigue").forGetter(FatigueStore::getReplenishedFatigue),
        Codec.INT.fieldOf("sideEffectStage").forGetter(FatigueStore::getSideEffectStage),
        Codec.BOOL.fieldOf("resetSpawn").forGetter(FatigueStore::getResetSpawn)
    ).apply(instance, FatigueStore::new));
    
    private double fatigue;
    private double extraFatigueRate;
    private double replenishedFatigue;
    private int fatigueUpdateCounter;
    private int sideEffectStage = -1;
    private boolean resetSpawn = true;
    private boolean sleepOverride;
    private boolean sleepNormally;
    private long wakeTime = -1;

    public FatigueStore() {}

    private FatigueStore(double fatigue, double extraFatigueRate, double replenishedFatigue, int sideEffectStage, boolean resetSpawn) {
        this.fatigue = fatigue;
        this.extraFatigueRate = extraFatigueRate;
        this.replenishedFatigue = replenishedFatigue;
        this.sideEffectStage = sideEffectStage;
        this.resetSpawn = resetSpawn;
    }

    private int getFatigueUpdateCounter() {
        return this.fatigueUpdateCounter;
    }

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
}
