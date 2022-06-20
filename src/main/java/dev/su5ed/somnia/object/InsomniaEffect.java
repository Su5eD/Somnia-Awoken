package dev.su5ed.somnia.object;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class InsomniaEffect extends MobEffect {
    public static final int COLOR = 0x23009a;

    public InsomniaEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }
}
