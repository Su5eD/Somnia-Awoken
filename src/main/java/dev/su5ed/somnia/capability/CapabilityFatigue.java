package dev.su5ed.somnia.capability;

import dev.su5ed.somnia.SomniaAwoken;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;

public final class CapabilityFatigue {
    public static final EntityCapability<Fatigue, Void> INSTANCE = EntityCapability.createVoid(new ResourceLocation(SomniaAwoken.MODID, "fatigue"), Fatigue.class);

    private CapabilityFatigue() {}
}
