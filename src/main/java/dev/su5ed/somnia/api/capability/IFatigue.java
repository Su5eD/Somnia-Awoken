package dev.su5ed.somnia.api.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IFatigue extends INBTSerializable<CompoundTag> {
    double getFatigue();

    void setFatigue(double fatigue);

    int getSideEffectStage();

    void setSideEffectStage(int stage);

    int updateFatigueCounter();

    void resetFatigueCounter();

    void maxFatigueCounter();

    void shouldResetSpawn(boolean resetSpawn);

    boolean resetSpawn();

    boolean sleepOverride();

    void setSleepOverride(boolean override);

    void setSleepNormally(boolean sleepNormally);

    boolean shouldSleepNormally();

    long getWakeTime();

    void setWakeTime(long wakeTime);

    double getExtraFatigueRate();

    void setExtraFatigueRate(double rate);

    double getReplenishedFatigue();

    void setReplenishedFatigue(double replenishedFatigue);
}
