package dev.su5ed.somnia.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public final class DarkUtilsPlugin {
    private static final ResourceLocation SLEEP_CHARM = new ResourceLocation(Compat.DARK_UTILS_MODID, "charm_sleep");

    public static boolean hasSleepCharm(Player player) {
        return Compat.curiosLoaded && CuriosPlugin.hasCurio(player, SLEEP_CHARM)
            || Compat.darkUtilsLoaded && Arrays.stream(EquipmentSlot.values())
                    .map(player::getItemBySlot)
                    .anyMatch(DarkUtilsPlugin::isSleepCharm)
                || player.getInventory().items.stream()
                    .anyMatch(DarkUtilsPlugin::isSleepCharm);
    }
    
    private static boolean isSleepCharm(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem().getRegistryName().equals(SLEEP_CHARM);
    }
    
    private DarkUtilsPlugin() {}
}
