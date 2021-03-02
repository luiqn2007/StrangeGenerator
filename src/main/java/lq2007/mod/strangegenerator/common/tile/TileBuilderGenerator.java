package lq2007.mod.strangegenerator.common.tile;

import lq2007.mod.strangegenerator.StrangeGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static lq2007.mod.strangegenerator.StrangeGenerator.TILE_ENTITIES;

public class TileBuilderGenerator extends BaseTileGenerator {

    public TileBuilderGenerator() {
        super(TILE_ENTITIES.get(TileBuilderGenerator.class), false);
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onPlace(BlockEvent.EntityPlaceEvent event) {
            IWorld world = event.getWorld();
            Entity entity = event.getEntity();
            if (event.isCanceled()) return;
            if (entity instanceof PlayerEntity && world instanceof ServerWorld && !world.isRemote()) {
                TileEntityType<BaseTileGenerator> type = TILE_ENTITIES.get(TileBuilderGenerator.class);
                for (BaseTileGenerator generator : BaseTileGenerator.getGenerators(type, (ServerWorld) world, entity.getUniqueID())) {
                    generator.receiveEnergy(5 /* todo configuration */);
                }
            }
        }

        // todo configuration if should remove
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onBreak(BlockEvent.BreakEvent event) {
            IWorld world = event.getWorld();
            PlayerEntity player = event.getPlayer();
            if (player == null || event.isCanceled()) return;
            if (world instanceof ServerWorld && !world.isRemote()) {
                TileEntityType<BaseTileGenerator> type = TILE_ENTITIES.get(TileBuilderGenerator.class);
                for (BaseTileGenerator generator : BaseTileGenerator.getGenerators(type, (ServerWorld) world, player.getUniqueID())) {
                    generator.removeEnergy(5 /* todo configuration */);
                }
            }
        }
    }
}
