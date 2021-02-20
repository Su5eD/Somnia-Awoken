package mods.su5ed.somnia.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.util.regex.Pattern;

public class Compat {
    private static final Pattern SLEEPING_BAG_PATTERN = Pattern.compile(".*_sleeping_bag");

    public static boolean isSleepingInBag(PlayerEntity player) {
        if (ModList.get().isLoaded("sleeping_bags")) {
            Item item = player.inventory.getCurrentItem().getItem();
            ResourceLocation name = item.getRegistryName();
            return name.getNamespace().equals("sleeping_bags") && SLEEPING_BAG_PATTERN.matcher(name.getPath()).find();
        }

        return false;
    }
}
