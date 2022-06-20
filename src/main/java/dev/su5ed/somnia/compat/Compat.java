package dev.su5ed.somnia.compat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.Optional;

public class Compat {
    public static boolean comforts;

    public static boolean isSleepingInHammock(PlayerEntity player) {
        if (comforts) {
            Optional<BlockPos> pos = player.getSleepingPos();
            if (pos.isPresent()) {
                Block block = player.level.getBlockState(pos.get()).getBlock();
                ResourceLocation regName = block.getRegistryName();
                return regName.getNamespace().equals("comforts") && regName.getPath().startsWith("hammock");
            }
        }
        return false;
    }

    public static boolean isSleepingInBag(PlayerEntity player) {
        Item item = player.inventory.getSelected().getItem();
        ResourceLocation name = item.getRegistryName();
        String namespace = name.getNamespace();
        String path = name.getPath();

        return namespace.equals("sleeping_bags") && path.endsWith("sleeping_bag") ||
                namespace.equals("comforts") && path.startsWith("sleeping_bag") ||
                namespace.equals("cyclic") && path.equals("sleeping_mat");
    }

    public static boolean isBed(BlockState state, BlockPos pos, IBlockReader world, LivingEntity sleeper) {
        if (comforts) {
            ResourceLocation regName = state.getBlock().getRegistryName();
            if (regName.getNamespace().equals("comforts") && regName.getPath().startsWith("hammock")) return false;
        }

        return state.isBed(world, pos, sleeper);
    }
}
