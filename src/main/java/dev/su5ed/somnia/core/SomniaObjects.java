package dev.su5ed.somnia.core;

import dev.su5ed.somnia.object.AwakeningEffect;
import dev.su5ed.somnia.object.InsomniaEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SomniaObjects {
    static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Somnia.MODID);
    static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Somnia.MODID);

    public static final RegistryObject<MobEffect> AWAKENING_EFFECT = MOB_EFFECTS.register("awakening", AwakeningEffect::new);
    public static final RegistryObject<MobEffect> INSOMNIA_EFFECT = MOB_EFFECTS.register("insomnia", InsomniaEffect::new);

    public static final RegistryObject<Potion> AWAKENING_POTION = POTIONS.register("awakening", () -> new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT.get(), 2400)));
    public static final RegistryObject<Potion> LONG_AWAKENING_POTION = POTIONS.register("long_awakening", () -> new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT.get(), 3600)));
    public static final RegistryObject<Potion> STRONG_AWAKENING_POTION = POTIONS.register("strong_awakening", () -> new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT.get(), 2400, 1)));

    public static final RegistryObject<Potion> INSOMNIA_POTION = POTIONS.register("insomnia", () -> new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT.get(), 1800)));
    public static final RegistryObject<Potion> LONG_INSOMNIA_POTION = POTIONS.register("long_insomnia", () -> new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT.get(), 3000)));
    public static final RegistryObject<Potion> STRONG_INSOMNIA_POTION = POTIONS.register("strong_insomnia", () -> new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT.get(), 1800, 1)));
    
    private SomniaObjects() {}
}
