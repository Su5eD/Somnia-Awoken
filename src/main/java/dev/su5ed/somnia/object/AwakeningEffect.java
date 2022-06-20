package dev.su5ed.somnia.object;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AwakeningEffect extends MobEffect {
    public static final int COLOR = 0x00ffee;

    public AwakeningEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
