package lq2007.mod.strangegenerator.common.tile;

import com.google.common.collect.ImmutableSet;
import lq2007.mod.strangegenerator.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TileInviteGenerator extends BaseTickableTileGenerator implements IInviteGenerator {

    private final Set<UUID> invitedPlayers = new HashSet<>();

    private int lastCount = 0, lastEnergy = 0;

    public TileInviteGenerator() {
        super(TileEntities.TILE_INVITE_GENERATOR.get(), false);
    }

    @Override
    public boolean isInvited(UUID uuid) {
        return invitedPlayers.contains(uuid);
    }

    @Override
    public void invite(UUID uuid) {
        invitedPlayers.add(uuid);
    }

    @Override
    public ImmutableSet<UUID> getInvitedIds() {
        return ImmutableSet.copyOf(invitedPlayers);
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
    public void placedBy(World worldIn, BlockPos pos, BlockState state, PlayerEntity placer, ItemStack stack) {
        super.placedBy(worldIn, pos, state, placer, stack);
        invitedPlayers.add(owner);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("generatorInviteIds", NBTUtils.writeUUIDs(invitedPlayers));
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        NBTUtils.readUUIDs(nbt, "generatorInviteIds", invitedPlayers, true);
    }
}
