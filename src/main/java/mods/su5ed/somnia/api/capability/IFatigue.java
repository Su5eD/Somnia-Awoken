package mods.su5ed.somnia.api.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IFatigue extends INBTSerializable<CompoundNBT> {
    double getFatigue();

    void setFatigue(double fatigue);

    int getSideEffectStage();

    void setSideEffectStage(int stage);

    int updateFatigueCounter();

    void resetFatigueCounter();

    void maxFatigueCounter();

    void shouldResetSpawn(boolean resetSpawn);

    boolean resetSpawn();
}
