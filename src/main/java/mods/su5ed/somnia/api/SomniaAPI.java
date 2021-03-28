package mods.su5ed.somnia.api;

import net.minecraft.item.Item;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SomniaAPI {
    private static final List<Pair<Item, Double>> REPLENISHING_ITEMS = new ArrayList<>();

    public static void addReplenishingItem(Item stack, double fatigueToReplenish) {
        REPLENISHING_ITEMS.add(Pair.of(stack, fatigueToReplenish));
    }

    public static List<Pair<Item, Double>> getReplenishingItems() {
        return new ArrayList<>(REPLENISHING_ITEMS);
    }
}
