package mods.su5ed.somnia.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class Compat {

    public static boolean isSleepingInBag(PlayerEntity player) {
        Item item = player.inventory.getCurrentItem().getItem();
        ResourceLocation name = item.getRegistryName();
        String namespace = name.getNamespace();
        String path = name.getPath();

        return namespace.equals("sleeping_bags") && path.endsWith("sleeping_bag") || namespace.equals("comforts") && path.startsWith("sleeping_bag");
    }
}
