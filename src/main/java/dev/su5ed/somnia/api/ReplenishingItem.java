package dev.su5ed.somnia.api;

import net.minecraft.world.item.Item;

public record ReplenishingItem(Item item, double replenishedFatigue, double fatigueRateModifier) {
}
