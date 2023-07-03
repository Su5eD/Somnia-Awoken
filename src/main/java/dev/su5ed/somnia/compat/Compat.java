package dev.su5ed.somnia.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class Compat {
    public static final String COMFORTS_MODID = "comforts";
    public static final String CURIOS_MODID = "curios";
    public static final String DARK_UTILS_MODID = "darkutils";

    public static final String COMFORTS_HAMMOCK = "hammock";

    public static boolean comfortsLoaded;
    public static boolean curiosLoaded;
    public static boolean darkUtilsLoaded;

    public static boolean isSleepingInHammock(Player player) {
        return comfortsLoaded && player.getSleepingPos()
            .map(pos -> {
                Block block = player.level().getBlockState(pos).getBlock();
                return ForgeRegistries.BLOCKS.getKey(block);
            })
            .map(name -> name.getNamespace().equals(COMFORTS_MODID) && name.getPath().startsWith(COMFORTS_HAMMOCK))
            .orElse(false);
    }

    public static boolean isBed(BlockState state, BlockPos pos, LevelAccessor world, LivingEntity sleeper) {
        if (comfortsLoaded) {
            ResourceLocation regName = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            if (regName.getNamespace().equals(COMFORTS_MODID) && regName.getPath().startsWith(COMFORTS_HAMMOCK)) return false;
        }

        return state.isBed(world, pos, sleeper);
    }

    private Compat() {}
}
