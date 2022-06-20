package dev.su5ed.somnia.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public final class Compat {
    public static final String COMFORTS_MODID = "comforts";
    public static final String SLEEPING_BAGS_MODID = "sleeping_bags";
    public static final String CYCLIC_MODID = "cyclic";
    public static final String CURIOS_MODID = "curios";
    public static final String DARK_UTILS_MODID = "darkutils";
    
    public static final String COMFORTS_HAMMOCK = "hammock";
    public static final String CYCLIC_SLEEPING_MAT = "sleeping_mat";
    public static final String SLEEPING_BAG = "sleeping_bag";
    
    public static boolean comfortsLoaded;
    public static boolean curiosLoaded;
    public static boolean darkUtilsLoaded;

    public static boolean isSleepingInHammock(Player player) {
        return comfortsLoaded && player.getSleepingPos()
            .map(pos -> player.level.getBlockState(pos).getBlock().getRegistryName())
            .map(name -> name.getNamespace().equals(COMFORTS_MODID) && name.getPath().startsWith(COMFORTS_HAMMOCK))
            .orElse(false);
    }

    public static boolean isSleepingInBag(Player player) {
        Item item = player.getInventory().getSelected().getItem();
        ResourceLocation name = item.getRegistryName();
        String modid = name.getNamespace();
        String path = name.getPath();

        return modid.equals(SLEEPING_BAGS_MODID) && path.endsWith(SLEEPING_BAG)
            || modid.equals(COMFORTS_MODID) && path.startsWith(SLEEPING_BAG)
            || modid.equals(CYCLIC_MODID) && path.equals(CYCLIC_SLEEPING_MAT);
    }

    public static boolean isBed(BlockState state, BlockPos pos, LevelAccessor world, LivingEntity sleeper) {
        if (comfortsLoaded) {
            ResourceLocation regName = state.getBlock().getRegistryName();
            if (regName.getNamespace().equals(COMFORTS_MODID) && regName.getPath().startsWith(COMFORTS_HAMMOCK)) return false;
        }

        return state.isBed(world, pos, sleeper);
    }
    
    private Compat() {}
}
