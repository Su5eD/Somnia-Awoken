package com.kingrunes.somnia.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityFatigue
{
    @CapabilityInject(IFatigue.class)
    public static Capability<IFatigue> FATIGUE_CAPABILITY = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IFatigue.class, new FatigueCapabilityStorage(), Fatigue.class);
    }
}
