package dev.su5ed.somnia.api;

import dev.su5ed.somnia.core.SomniaConfig;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class SomniaAPI {
    private static final List<ReplenishingItem> REPLENISHING_ITEMS = new ArrayList<>();

    public static void addReplenishingItem(Item item, double fatigueToReplenish) {
        addReplenishingItem(item, fatigueToReplenish, SomniaConfig.COMMON.fatigueRate.get());
    }

    public static void addReplenishingItem(Item item, double fatigueToReplenish, double fatigueRateModifier) {
        REPLENISHING_ITEMS.add(new ReplenishingItem(item, fatigueToReplenish, fatigueRateModifier));
    }

    public static List<ReplenishingItem> getReplenishingItems() {
        return new ArrayList<>(REPLENISHING_ITEMS);
    }
    
    private SomniaAPI() {}
}
