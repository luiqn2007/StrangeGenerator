package lq2007.mod.strangegenerator.common.block;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lq2007.mod.strangegenerator.common.capability.Capabilities;
import lq2007.mod.strangegenerator.common.tile.NoTileGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

// todo allow owner = null
// todo moving to save
public class PistonGenerator extends BaseGenerator<NoTileGenerator> {

    /**
     * Table0: World - PistonPosition - Generator
     * EXTEND: Replaced by MovingPistonBlock, Add replace MovingPistonBlock
     * RETRACT: Replaced by AirBlock, Add replace MovingPistonBlock
     */
    public static final Table<World, BlockPos, MovingGenerator> MOVING_GENERATORS0 = HashBasedTable.create();
    /**
     * Table1: World - GeneratorPosition - Generator
     */
    public static final Table<World, BlockPos, MovingGenerator> MOVING_GENERATORS1 = HashBasedTable.create();

    @Override
    public void doDebug(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack, BlockRayTraceResult hitTrace) {

    }

    @Override
    protected boolean canPlace(BlockItemUseContext context) {
        World world = context.getWorld();
        if (!world.isRemote) {
            PlayerEntity player = context.getPlayer();
            if (player == null) return false;
//        return Capabilities.getWorldData(world).getPistonGeneratorCount(player) < 1 /* todo configuration */;
            return true;
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote && placer instanceof PlayerEntity) {
            Capabilities.getWorldData(worldIn).newPistonGenerator(pos, (PlayerEntity) placer);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
        if (!worldIn.isRemote) {
            MovingGenerator mg = MOVING_GENERATORS1.remove(worldIn, pos);
            if (isMoving && mg != null) {
                MOVING_GENERATORS0.remove(worldIn, mg.piston);
                Capabilities.getWorldData(worldIn).addPistonGenerator(pos, mg.owner, mg.storage);
            } else {
                Capabilities.getWorldData(worldIn).newPistonGenerator(pos, null);
            }
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onReplaced(state, worldIn, pos, newState, isMoving);
        if (!worldIn.isRemote) {
            Capabilities.getWorldData(worldIn).removePistonGenerator(pos);
        }
    }

    private static class MovingGenerator {
        UUID owner;
        EnergyStorage storage;
        BlockPos piston, target;

        public MovingGenerator(UUID owner, EnergyStorage storage, BlockPos piston, BlockPos target) {
            this.owner = owner;
            this.storage = storage;
            this.piston = piston;
            this.target = target;
        }

        public void addEnergy() {
            storage.receiveEnergy(100 /* todo configuration */, false);
        }
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void prePistonMove(PistonEvent.Pre event) {
            IWorld world = event.getWorld();
            if (!world.isRemote() && world instanceof World) {
                BlockPos piston = event.getPos();
                Direction direction = event.getDirection();
                BlockPos block, target;
                switch (event.getPistonMoveType()) {
                    case EXTEND: {
                        if (world.getBlockState(piston).getBlock() == Blocks.STICKY_PISTON || world.getBlockState(piston).getBlock() == Blocks.PISTON) {
                            block = event.getFaceOffsetPos();
                            target = block.offset(direction);
                            break;
                        } else return;
                    }
                    case RETRACT: {
                        if (world.getBlockState(piston).getBlock() == Blocks.STICKY_PISTON) {
                            target = event.getFaceOffsetPos();
                            block = target.offset(direction);
                            break;
                        } else return;
                    }
                    default:
                        return;
                }
                World w = (World) world;
                Capabilities.getWorldData(w).getPistonGenerator(block).ifPresent(generator -> {
                    // todo final pos error: back
                    MovingGenerator mg = new MovingGenerator(generator.owner, generator.storage, piston, target);
                    MOVING_GENERATORS0.put(w, piston, mg);
                    MOVING_GENERATORS1.put(w, target, mg);
                });
            }
        }

        @SubscribeEvent
        public static void postPistonMove(PistonEvent.Post event) {
            BlockPos pos = event.getPos();
            IWorld world = event.getWorld();
            if (!world.isRemote() && world instanceof World) {
                World w = (World) world;
                MovingGenerator generator = MOVING_GENERATORS0.get(w, pos);
                if (generator != null) {
                    generator.addEnergy();
                }
            }
        }
    }
}
