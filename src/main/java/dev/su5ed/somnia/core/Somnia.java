package dev.su5ed.somnia.core;

import dev.su5ed.somnia.compat.Compat;
import dev.su5ed.somnia.handler.ClientTickHandler;
import dev.su5ed.somnia.network.SomniaNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Somnia.MODID)
public class Somnia {
    public static final String MODID = "somnia";
    public static final Logger LOGGER = LogManager.getLogger();

    public Somnia() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(ClientTickHandler.INSTANCE));

        SomniaObjects.MOB_EFFECTS.register(bus);
        SomniaObjects.POTIONS.register(bus);

        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, SomniaConfig.COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, SomniaConfig.CLIENT_SPEC);
    }

    private void setup(final FMLCommonSetupEvent event) {
        SomniaNetwork.registerMessages();

        Compat.comfortsLoaded = ModList.get().isLoaded(Compat.COMFORTS_MODID);
        Compat.curiosLoaded = ModList.get().isLoaded(Compat.CURIOS_MODID);
        Compat.darkUtilsLoaded = ModList.get().isLoaded(Compat.DARK_UTILS_MODID);
        
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
}
