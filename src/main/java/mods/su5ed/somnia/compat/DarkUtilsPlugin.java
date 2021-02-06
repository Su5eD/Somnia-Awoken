package mods.su5ed.somnia.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

public class DarkUtilsPlugin {
    public static final ResourceLocation SLEEP_CHARM = new ResourceLocation("darkutils", "charm_sleep");

    public static boolean hasSleepCharm(PlayerEntity player) {
        if (ModList.get().isLoaded("curios") && CuriosPlugin.hasCurio(player, SLEEP_CHARM)) return true;

        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            ItemStack stack = player.getItemStackFromSlot(slot);
            if (!stack.isEmpty() && stack.getItem().getRegistryName().equals(SLEEP_CHARM)) return true;
        }

        for(ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem().getRegistryName().equals(SLEEP_CHARM)) return true;
        }

        return false;
    }
}
