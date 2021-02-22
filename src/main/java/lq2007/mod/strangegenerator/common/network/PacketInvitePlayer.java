package lq2007.mod.strangegenerator.common.network;

import com.google.common.collect.ImmutableList;
import lq2007.mod.strangegenerator.common.data.Player;
import lq2007.mod.strangegenerator.common.tile.TileEntities;
import lq2007.mod.strangegenerator.common.tile.TileInviteGenerator;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class PacketInvitePlayer extends BasePacketInvitePlayers<TileInviteGenerator> {

    public PacketInvitePlayer(boolean allWorld, boolean skipAccepted, boolean onlyAccepted, @Nullable ResourceLocation world, ResourceLocation teWorld, BlockPos pos) {
        super(allWorld, skipAccepted, onlyAccepted, world, teWorld, pos);
    }

    public PacketInvitePlayer(boolean allWorld, boolean skipAccepted, boolean onlyAccepted, @Nullable ResourceLocation world, ResourceLocation teWorld, BlockPos pos, ImmutableList<Player.CurrentStatus> players) {
        super(allWorld, skipAccepted, onlyAccepted, world, teWorld, pos, players);
    }

    public PacketInvitePlayer(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected TileEntityType<TileInviteGenerator> getGeneratorType() {
        return TileEntities.TILE_INVITE_GENERATOR.get();
    }

    @Override
    protected BasePacketInvitePlayers<TileInviteGenerator> withInvitations(ImmutableList<Player.CurrentStatus> invitations) {
        return new PacketInvitePlayer(allWorld, skipAccepted, onlyAccepted, world, teWorld, pos, invitations);
    }
}
