package lq2007.mod.strangegenerator.common.network;

import com.google.common.collect.ImmutableList;
import lq2007.mod.strangegenerator.common.data.Player;
import lq2007.mod.strangegenerator.common.tile.TileDeadGenerator;
import lq2007.mod.strangegenerator.common.tile.TileEntities;
import lq2007.mod.strangegenerator.common.tile.TileInviteGenerator;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class PacketDeadPlayer extends BasePacketInvitePlayers<TileDeadGenerator> {

    public PacketDeadPlayer(boolean allWorld, boolean skipAccepted, boolean onlyAccepted, @Nullable ResourceLocation world, ResourceLocation teWorld, BlockPos pos) {
        super(allWorld, skipAccepted, onlyAccepted, world, teWorld, pos);
    }

    public PacketDeadPlayer(boolean allWorld, boolean skipAccepted, boolean onlyAccepted, @Nullable ResourceLocation world, ResourceLocation teWorld, BlockPos pos, ImmutableList<Player.CurrentStatus> players) {
        super(allWorld, skipAccepted, onlyAccepted, world, teWorld, pos, players);
    }

    public PacketDeadPlayer(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected TileEntityType<TileDeadGenerator> getGeneratorType() {
        return TileEntities.TILE_DEAD_GENERATOR.get();
    }

    @Override
    protected BasePacketInvitePlayers<TileDeadGenerator> withInvitations(ImmutableList<Player.CurrentStatus> invitations) {
        return new PacketDeadPlayer(allWorld, skipAccepted, onlyAccepted, world, teWorld, pos, invitations);
    }
}
