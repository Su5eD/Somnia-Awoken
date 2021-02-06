package mods.su5ed.somnia.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public class CuriosPlugin {

    public static boolean hasCurio(PlayerEntity player, ResourceLocation name) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> curio = CuriosApi.getCuriosHelper().findEquippedCurio(stack -> stack.getItem().getRegistryName().equals(name), player);
        return curio.isPresent() && !curio.get().right.isEmpty();
    }
}
