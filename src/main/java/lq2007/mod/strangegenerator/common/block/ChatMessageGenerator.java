package lq2007.mod.strangegenerator.common.block;

import lq2007.mod.strangegenerator.common.tile.TileChatMessageGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/**
 * Generate power according to the content of chat information
 */
public class ChatMessageGenerator extends BaseGenerator<TileChatMessageGenerator> {

    @Override
    public void doDebug(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack, BlockRayTraceResult hitTrace) {
        // todo debug ChatMessageGenerator
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context) {
        // todo configuration
        return placeByPlayer(context, 1, false);
    }
}
