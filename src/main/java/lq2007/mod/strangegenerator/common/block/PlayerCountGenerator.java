package lq2007.mod.strangegenerator.common.block;

import lq2007.mod.strangegenerator.common.capability.Capabilities;
import lq2007.mod.strangegenerator.common.tile.BaseTileGenerator;
import lq2007.mod.strangegenerator.common.tile.TileEntities;
import lq2007.mod.strangegenerator.common.tile.TilePlayerCountGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/**
 * Generate power according to the number of players.
 * @see TilePlayerCountGenerator
 */
public class PlayerCountGenerator extends BaseGenerator<TilePlayerCountGenerator> {

    @Override
    public void doDebug(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack, BlockRayTraceResult hitTrace) {
        // todo debug PlayerCountGenerator
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context) {
        return placeByPlayer(context, TileEntities.TILE_PLAYER_COUNT_GENERATOR, 1, false);
    }
}
