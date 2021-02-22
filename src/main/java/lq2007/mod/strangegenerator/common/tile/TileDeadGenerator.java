package lq2007.mod.strangegenerator.common.tile;

import com.google.common.collect.ImmutableSet;
import lq2007.mod.strangegenerator.common.data.Player;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

// todo power during dead
public class TileDeadGenerator extends BaseTileGenerator implements IInviteGenerator {

    private final Map<UUID, Player> watchedPlayer = new HashMap<>();

    public TileDeadGenerator() {
        super(TileEntities.TILE_DEAD_GENERATOR.get(), false);
    }

    @Override
    public boolean isInvited(UUID uuid) {
        return watchedPlayer.containsKey(uuid);
    }

    @Override
    public void invite(UUID uuid) {
        watchedPlayer.put(uuid, new Player(uuid));
    }

    @Override
    public void invite(PlayerEntity player) {
        watchedPlayer.put(player.getUniqueID(), new Player(player));
    }

    @Override
    public ImmutableSet<Player> getInvitedPlayers() {
        return ImmutableSet.copyOf(watchedPlayer.values());
    }

    @Override
    public ImmutableSet<UUID> getInvitedIds() {
        return ImmutableSet.copyOf(watchedPlayer.keySet());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        writeInvitedPlayersToNbt(compound, "watchedIds", watchedPlayer);
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        readInvitedPlayersFromNbt(nbt, "watchedIds", watchedPlayer);
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
