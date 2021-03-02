package lq2007.mod.strangegenerator.common.tile;

import com.google.common.collect.ImmutableSet;
import lq2007.mod.strangegenerator.StrangeGenerator;
import lq2007.mod.strangegenerator.common.data.Player;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TileInviteGenerator extends BaseTickableTileGenerator implements IInviteGenerator {

    private final Map<UUID, Player> invitedPlayers = new HashMap<>();

    private int lastCount = 0, lastEnergy = 0;

    public TileInviteGenerator() {
        super(StrangeGenerator.TILE_ENTITIES.get(TileInviteGenerator.class), false);
    }

    @Override
    public boolean isInvited(UUID uuid) {
        return invitedPlayers.containsKey(uuid);
    }

    @Override
    public void invite(UUID uuid) {
        invitedPlayers.put(uuid, new Player(uuid));
    }

    @Override
    public void invite(PlayerEntity player) {
        invitedPlayers.put(player.getUniqueID(), new Player(player));
    }

    @Override
    public ImmutableSet<Player> getInvitedPlayers() {
        return ImmutableSet.copyOf(invitedPlayers.values());
    }

    @Override
    public ImmutableSet<UUID> getInvitedIds() {
        return ImmutableSet.copyOf(invitedPlayers.keySet());
    }

    @Override
    protected void update(boolean isServer) {
        if (isServer && !invitedPlayers.isEmpty()) {
            if (invitedPlayers.size() != lastCount) {
                // todo configuration
                lastCount = invitedPlayers.size();
                lastEnergy = (int) (lastCount * 1.5);
            }
            receiveEnergy(lastEnergy);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        writeInvitedPlayersToNbt(compound, "generatorInviteIds", invitedPlayers);
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        readInvitedPlayersFromNbt(nbt, "generatorInviteIds", invitedPlayers);
    }
}
