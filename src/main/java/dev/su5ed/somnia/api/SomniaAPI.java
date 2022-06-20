package dev.su5ed.somnia.api;

import dev.su5ed.somnia.config.SomniaConfig;
import net.minecraft.item.Item;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

public class SomniaAPI {
    private static final List<Triple<Item, Double, Double>> REPLENISHING_ITEMS = new ArrayList<>();

    public static void addReplenishingItem(Item stack, double fatigueToReplenish) {
        addReplenishingItem(stack, fatigueToReplenish, SomniaConfig.fatigueRate);
    }

    public static void addReplenishingItem(Item stack, double fatigueToReplenish, double fatigueRateModifier) {
        REPLENISHING_ITEMS.add(Triple.of(stack, fatigueToReplenish, fatigueRateModifier));
    }

    public static List<Triple<Item, Double, Double>> getReplenishingItems() {
        return new ArrayList<>(REPLENISHING_ITEMS);
    }
}
