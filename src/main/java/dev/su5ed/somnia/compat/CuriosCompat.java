package dev.su5ed.somnia.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosCompat {

    public static boolean hasCurio(Player player, ResourceLocation name) {
        return CuriosApi.getCuriosInventory(player)
            .flatMap(inv -> inv.findFirstCurio(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(name)))
            .map(curio -> !curio.stack().isEmpty())
            .orElse(false);
    }

    private CuriosCompat() {}
}
