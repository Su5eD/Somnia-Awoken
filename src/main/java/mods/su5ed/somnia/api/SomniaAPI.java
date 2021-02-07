package mods.su5ed.somnia.api;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SomniaAPI {
    private static final List<Pair<ItemStack, Double>> COFFEES = new ArrayList<>();

    public static void addCoffee(ItemStack stack, double fatigueToReplenish) {
        COFFEES.add(Pair.of(stack, fatigueToReplenish));
    }

    public static List<Pair<ItemStack, Double>> getCoffeeList() {
        return new ArrayList<>(COFFEES);
    }
}
