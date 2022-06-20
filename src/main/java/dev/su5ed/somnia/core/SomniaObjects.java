package dev.su5ed.somnia.core;

import dev.su5ed.somnia.object.AwakeningEffect;
import dev.su5ed.somnia.object.InsomniaEffect;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SomniaObjects {
    static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, Somnia.MODID);
    static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTION_TYPES, Somnia.MODID);

    public static final RegistryObject<Effect> AWAKENING_EFFECT = EFFECTS.register("awakening", AwakeningEffect::new);
    public static final RegistryObject<Effect> INSOMNIA_EFFECT = EFFECTS.register("insomnia", InsomniaEffect::new);

    public static final RegistryObject<Potion> AWAKENING_POTION = POTIONS.register("awakening", () -> new Potion("awakening", new EffectInstance(AWAKENING_EFFECT.get(), 2400)));
    public static final RegistryObject<Potion> LONG_AWAKENING_POTION = POTIONS.register("long_awakening", () -> new Potion("awakening", new EffectInstance(AWAKENING_EFFECT.get(), 3600)));
    public static final RegistryObject<Potion> STRONG_AWAKENING_POTION = POTIONS.register("strong_awakening", () -> new Potion("awakening", new EffectInstance(AWAKENING_EFFECT.get(), 2400, 1)));

    public static final RegistryObject<Potion> INSOMNIA_POTION = POTIONS.register("insomnia", () -> new Potion("insomnia", new EffectInstance(INSOMNIA_EFFECT.get(), 1800)));
    public static final RegistryObject<Potion> LONG_INSOMNIA_POTION = POTIONS.register("long_insomnia", () -> new Potion("insomnia", new EffectInstance(INSOMNIA_EFFECT.get(), 3000)));
    public static final RegistryObject<Potion> STRONG_INSOMNIA_POTION = POTIONS.register("strong_insomnia", () -> new Potion("insomnia", new EffectInstance(INSOMNIA_EFFECT.get(), 1800, 1)));
}
