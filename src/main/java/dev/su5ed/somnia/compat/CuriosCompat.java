package dev.su5ed.somnia.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosCompat {

    public static boolean hasCurio(Player player, ResourceLocation name) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, stack -> stack.getItem().getRegistryName().equals(name))
            .map(curio -> !curio.stack().isEmpty())
            .orElse(false);
    }
    
    private CuriosCompat() {}
}
