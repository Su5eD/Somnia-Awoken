package dev.su5ed.somnia.api.capability;

import dev.su5ed.somnia.core.Somnia;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityFatigue {
    @CapabilityInject(IFatigue.class)
    public static Capability<IFatigue> FATIGUE_CAPABILITY = null;
    public static final ResourceLocation NAME = new ResourceLocation(Somnia.MODID, "fatigue");

    public static void register() {
        CapabilityManager.INSTANCE.register(IFatigue.class, new CapabilityFatigueStorage(), Fatigue::new);
    }
}
