package lq2007.mod.strangegenerator.common.item;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

public class OwnerBlockItem<T extends Block> extends BlockItem {

    public static Table<World, BlockPos, PlayerEntity> OWNER_SERVER = HashBasedTable.create();
    public static Table<World, BlockPos, PlayerEntity> OWNER_CLIENT = HashBasedTable.create();

    public static PlayerEntity findPlacer(World world, BlockPos pos) {
        return (world.isRemote ? OWNER_CLIENT : OWNER_SERVER).get(world, pos);
    }

    public OwnerBlockItem(RegistryObject<T> obj) {
        super(obj.get(), new Properties().group(Items.GROUP_GENERATOR).maxStackSize(1));
    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return false;
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Table<World, BlockPos, PlayerEntity> table = world.isRemote ? OWNER_CLIENT : OWNER_SERVER;
        table.put(world, pos, player);
        boolean placeBlock = super.placeBlock(context, state);
        table.remove(world, pos);
        return placeBlock;
    }
}
