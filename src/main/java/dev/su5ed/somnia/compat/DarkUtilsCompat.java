package dev.su5ed.somnia.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

public final class DarkUtilsCompat {
    private static final ResourceLocation SLEEP_CHARM = new ResourceLocation(Compat.DARK_UTILS_MODID, "charm_sleep");

    public static boolean hasSleepCharm(Player player) {
        return Compat.curiosLoaded && CuriosCompat.hasCurio(player, SLEEP_CHARM)
            || Compat.darkUtilsLoaded && Arrays.stream(EquipmentSlot.values())
                    .map(player::getItemBySlot)
                    .anyMatch(DarkUtilsCompat::isSleepCharm)
                || player.getInventory().items.stream()
                    .anyMatch(DarkUtilsCompat::isSleepCharm);
    }
    
    private static boolean isSleepCharm(ItemStack stack) {
        return !stack.isEmpty() && ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(SLEEP_CHARM);
    }
    
    private DarkUtilsCompat() {}
}
