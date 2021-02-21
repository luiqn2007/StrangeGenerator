package lq2007.mod.strangegenerator.common.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

public class TileLoveGenerator extends BaseTileGenerator {

    private AxisAlignedBB watchedRange = new AxisAlignedBB(BlockPos.ZERO);

    public TileLoveGenerator() {
        super(TileEntities.TILE_LOVE_GENERATOR.get(), false);
    }

    private float getRange() {
        // todo configuration
        return 10;
    }

    public boolean isAnimalWatched(AnimalEntity entity) {
        return watchedRange.contains(entity.getPosX(), entity.getPosY(), entity.getPosZ());
    }

    @Override
    public void placedBy(World worldIn, BlockPos pos, BlockState state, PlayerEntity placer, ItemStack stack) {
        super.placedBy(worldIn, pos, state, placer, stack);
        float range = getRange();
        watchedRange = AxisAlignedBB.withSizeAtOrigin(range, range, range).offset(pos);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        float range = getRange();
        watchedRange = AxisAlignedBB.withSizeAtOrigin(range, range, range).offset(pos);
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
            TileEntityType<TileLoveGenerator> type = TileEntities.TILE_LOVE_GENERATOR.get();
            LivingEntity entity = event.getEntityLiving();
            if (entity.world != null && !entity.world.isRemote && entity instanceof AnimalEntity && entity.isAlive()) {
                AnimalEntity animal = (AnimalEntity) entity;
                if (animal.isInLove()) {
                    ServerPlayerEntity cause = animal.getLoveCause();
                    if (cause == null) {
                        animalDoLove(animal, BaseTileGenerator.getGenerators(type, (ServerWorld) entity.world));
                    } else {
                        animalDoLove(animal, BaseTileGenerator.getGenerators(type, cause.getServerWorld(), cause.getUniqueID()));
                    }
                }
            }
        }

        private static void animalDoLove(AnimalEntity animal, Set<TileLoveGenerator> generators) {
            for (TileLoveGenerator generator : generators) {
                if (generator.isAnimalWatched(animal)) {
                    generator.receiveEnergy(10 /* todo configuration */);
                    if (animal.world.rand.nextFloat() <= 0.5F /* todo configuration */) {
                        animal.resetInLove();
                        break;
                    }
                }
            }
        }
    }
}
