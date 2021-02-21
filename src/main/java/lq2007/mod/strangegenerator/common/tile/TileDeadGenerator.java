package lq2007.mod.strangegenerator.common.tile;

import com.google.common.collect.ImmutableSet;
import lq2007.mod.strangegenerator.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// todo power during dead
public class TileDeadGenerator extends BaseTileGenerator implements IInviteGenerator {

    private final List<UUID> watchedPlayerIds = new ArrayList<>();

    public TileDeadGenerator() {
        super(TileEntities.TILE_DEAD_GENERATOR.get(), false);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        NBTUtils.readUUIDs(nbt, "watchedIds", watchedPlayerIds, true);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("watchedIds", NBTUtils.writeUUIDs(watchedPlayerIds));
        return compound;
    }

    @Override
    public boolean isInvited(UUID uuid) {
        return watchedPlayerIds.contains(uuid);
    }

    @Override
    public void invite(UUID uuid) {
        watchedPlayerIds.add(uuid);
    }

    @Override
    public ImmutableSet<UUID> getInvitedIds() {
        return ImmutableSet.copyOf(watchedPlayerIds);
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void onPlayerDead(LivingDeathEvent event) {
            Entity entity = event.getEntity();
            World world = entity.world;
            if (!event.isCanceled() && entity instanceof PlayerEntity && world instanceof ServerWorld) {
                TileEntityType<TileDeadGenerator> type = TileEntities.TILE_DEAD_GENERATOR.get();
                UUID deadPlayer = entity.getUniqueID();
                for (TileDeadGenerator generator : BaseTileGenerator.getGenerators(type)) {
                    if (generator.isInvited(deadPlayer)) {
                        generator.receiveEnergy(1000 /* todo configuration */);
                    }
                }
            }
        }
    }
}
