package lq2007.mod.strangegenerator.common.block;

import lq2007.mod.strangegenerator.StrangeGenerator;
import lq2007.mod.strangegenerator.common.item.ItemDebug;
import lq2007.mod.strangegenerator.common.tile.BaseTileGenerator;
import lq2007.mod.strangegenerator.common.tile.NoTileGenerator;
import lq2007.mod.strangegenerator.register.ICustomItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseGenerator<T extends BaseTileGenerator> extends Block implements ICustomItem {

    private static final Logger LOGGER = LogManager.getLogger();

    protected Class<T> tileClass = null;
    protected Constructor<T> creator = null;
    protected boolean hasTileClass = true;

    public BaseGenerator() {
        super(Properties.create(Material.IRON));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return canPlace(context) ? super.getStateForPlacement(context) : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        setTileClass();
        return hasTileClass;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        setTileClass();
        if (creator != null) {
            try {
                return creator.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        }
        return null;
    }

    protected void setTileClass() {
        if (hasTileClass) {
            if (tileClass == null) {
                hasTileClass = false;
                Type superclass = getClass().getGenericSuperclass();
                if (superclass instanceof ParameterizedType) {
                    Type[] arguments = ((ParameterizedType) superclass).getActualTypeArguments();
                    if (arguments.length > 0) {
                        Type type = arguments[0];
                        if (type instanceof Class<?>) {
                            tileClass = (Class<T>) type;
                            if (tileClass == NoTileGenerator.class) return;
                            try {
                                creator = tileClass.getConstructor();
                                creator.setAccessible(false);
                                hasTileClass = true;
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack heldItem = player.getHeldItem(handIn);
        if (heldItem.getItem() == StrangeGenerator.ITEMS.get(ItemDebug.class)) {
            doDebug(worldIn, pos, state, player, heldItem, hit);
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (hasTileClass && (placer instanceof PlayerEntity)) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof BaseTileGenerator) {
                ((BaseTileGenerator) entity).placedBy(worldIn, pos, state, (PlayerEntity) placer, stack);
            } else {
                LOGGER.warn("Can't find a BaseTileGenerator at {}", pos);
            }
        }
    }

    public abstract void doDebug(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack, BlockRayTraceResult hitTrace);

    protected abstract boolean canPlace(BlockItemUseContext context);

    protected boolean placeByPlayer(BlockItemUseContext context, int maxCount, boolean allWorld) {
        setTileClass();
        if (hasTileClass) {
            PlayerEntity player = context.getPlayer();
            if (player == null || !(player.world instanceof ServerWorld)) return false;
            // todo configuration 0
            TileEntityType<?> type = StrangeGenerator.TILE_ENTITIES.get(tileClass);
            if (allWorld) {
                return BaseTileGenerator.getGeneratorCount(type, player.getUniqueID()) < maxCount;
            } else {
                return BaseTileGenerator.getGeneratorCount(type, (ServerWorld) player.world, player.getUniqueID()) < maxCount;
            }
        }
        return true;
    }

    @Override
    public BlockItem newBlockItem() {
        return new OwnerBlockItem<>(StrangeGenerator.BLOCKS.get(getClass()));
    }
}
