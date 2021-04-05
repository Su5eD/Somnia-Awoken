package mods.su5ed.somnia.core;

import mods.su5ed.somnia.object.AwakeningEffect;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SomniaObjects {
    static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, Somnia.MODID);

    public static final RegistryObject<Effect> AWAKENING_EFFECT = EFFECTS.register("awakening", AwakeningEffect::new);

    static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTION_TYPES, Somnia.MODID);

    public static final RegistryObject<Potion> AWAKENING_POTION = POTIONS.register("awakening", () -> new Potion("awakening", new EffectInstance(AWAKENING_EFFECT.get(), 2400)));
    public static final RegistryObject<Potion> LONG_AWAKENING_POTION = POTIONS.register("long_awakening", () -> new Potion("awakening", new EffectInstance(AWAKENING_EFFECT.get(), 3600)));
    public static final RegistryObject<Potion> STRONG_AWAKENING_POTION = POTIONS.register("strong_awakening", () -> new Potion("awakening", new EffectInstance(AWAKENING_EFFECT.get(), 2400, 1)));
}
