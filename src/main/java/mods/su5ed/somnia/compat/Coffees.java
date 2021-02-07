package mods.su5ed.somnia.compat;

import mods.su5ed.somnia.api.SomniaAPI;
import mods.su5ed.somnia.core.Somnia;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class Coffees {
    public static void registerCoffees() {
        Somnia.LOGGER.info("Adding support for various coffees from other mods");
        ModList list = ModList.get();
        if (list.isLoaded("coffeespawner")) {
            SomniaAPI.addCoffee(getModItem("coffeespawner", "coffee"), 10);
            SomniaAPI.addCoffee(getModItem("coffeespawner", "coffee_milk"), 10);
            SomniaAPI.addCoffee(getModItem("coffeespawner", "coffee_sugar"), 15);
            SomniaAPI.addCoffee(getModItem("coffeespawner", "coffee_milk_sugar"), 15);
        }
        if (list.isLoaded("coffeemod")) {
            SomniaAPI.addCoffee(getModItem("coffeemod", "coffee"), 15);
            SomniaAPI.addCoffee(getModItem("coffeemod", "espresso"), 15);
            SomniaAPI.addCoffee(getModItem("coffeemod", "latte"), 15);

            SomniaAPI.addCoffee(getModItem("coffeemod", "caramel_macchiato"), 10);
            SomniaAPI.addCoffee(getModItem("coffeemod", "mocha"), 10);
            SomniaAPI.addCoffee(getModItem("coffeemod", "frappe"), 10);

            SomniaAPI.addCoffee(getModItem("coffeemod", "cocoa_drink"), 5);
        }
    }

    public static ItemStack getModItem(String modid, String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, name));
        if (item != null) return new ItemStack(item, 1);
        return ItemStack.EMPTY;
    }
}
