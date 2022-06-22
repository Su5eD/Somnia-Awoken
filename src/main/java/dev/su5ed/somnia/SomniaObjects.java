package dev.su5ed.somnia;

import dev.su5ed.somnia.effect.AwakeningEffect;
import dev.su5ed.somnia.effect.InsomniaEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SomniaObjects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Somnia.MODID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Somnia.MODID);

    public static final RegistryObject<MobEffect> AWAKENING_EFFECT = MOB_EFFECTS.register("awakening", AwakeningEffect::new);
    public static final RegistryObject<MobEffect> INSOMNIA_EFFECT = MOB_EFFECTS.register("insomnia", InsomniaEffect::new);

    public static final RegistryObject<Potion> AWAKENING_POTION = POTIONS.register("awakening", () -> new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT.get(), 2400)));
    public static final RegistryObject<Potion> LONG_AWAKENING_POTION = POTIONS.register("long_awakening", () -> new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT.get(), 3600)));
    public static final RegistryObject<Potion> STRONG_AWAKENING_POTION = POTIONS.register("strong_awakening", () -> new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT.get(), 2400, 1)));

    public static final RegistryObject<Potion> INSOMNIA_POTION = POTIONS.register("insomnia", () -> new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT.get(), 1800)));
    public static final RegistryObject<Potion> LONG_INSOMNIA_POTION = POTIONS.register("long_insomnia", () -> new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT.get(), 3000)));
    public static final RegistryObject<Potion> STRONG_INSOMNIA_POTION = POTIONS.register("strong_insomnia", () -> new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT.get(), 1800, 1)));
    
    static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
        POTIONS.register(bus);
    }
    
    static void registerBrewingRecipes() {
        addBrewingRecipe(Potions.NIGHT_VISION, Items.GLISTERING_MELON_SLICE, SomniaObjects.AWAKENING_POTION.get());
        addBrewingRecipe(Potions.LONG_NIGHT_VISION, Items.GLISTERING_MELON_SLICE, SomniaObjects.LONG_AWAKENING_POTION.get());
        addBrewingRecipe(Potions.NIGHT_VISION, Items.BLAZE_POWDER, SomniaObjects.STRONG_AWAKENING_POTION.get());
        
        addBrewingRecipe(SomniaObjects.AWAKENING_POTION.get(), Items.FERMENTED_SPIDER_EYE, SomniaObjects.INSOMNIA_POTION.get());
        addBrewingRecipe(SomniaObjects.LONG_AWAKENING_POTION.get(), Items.FERMENTED_SPIDER_EYE, SomniaObjects.LONG_INSOMNIA_POTION.get());
        addBrewingRecipe(SomniaObjects.STRONG_AWAKENING_POTION.get(), Items.FERMENTED_SPIDER_EYE, SomniaObjects.STRONG_INSOMNIA_POTION.get());
    }
    
    private static void addBrewingRecipe(Potion input, Item ingredient, Potion output) {
        ItemStack outputStack = new ItemStack(Items.POTION);
        PotionUtils.setPotion(outputStack, output);
        BrewingRecipeRegistry.addRecipe(createPotionIngredient(input), Ingredient.of(ingredient), outputStack);
    }

    private static Ingredient createPotionIngredient(Potion potion) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Potion", potion.getRegistryName().toString());
        return PartialNBTIngredient.of(Items.POTION, tag);
    }
    
    private SomniaObjects() {}
}
