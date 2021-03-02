package lq2007.mod.strangegenerator.common.network;

import lq2007.mod.strangegenerator.StrangeGenerator;
import lq2007.mod.strangegenerator.common.data.InvitationMap;
import lq2007.mod.strangegenerator.common.tile.TileInviteGenerator;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PacketInviteInvitations extends BasePacketInvitations<TileInviteGenerator> {

    public PacketInviteInvitations(boolean allWorld, boolean skipAccepted, boolean skipSelf, boolean onlyAccepted, boolean onlySelf, @Nullable World world) {
        super(allWorld, skipAccepted, skipSelf, onlyAccepted, onlySelf, world);
    }

    public PacketInviteInvitations(boolean allWorld, boolean skipAccepted, boolean skipSelf, boolean onlyAccepted, boolean onlySelf, @Nullable ResourceLocation world, InvitationMap invitations) {
        super(allWorld, skipAccepted, skipSelf, onlyAccepted, onlySelf, world, invitations);
    }

    public PacketInviteInvitations(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected TileEntityType<TileInviteGenerator> getGeneratorType() {
        return StrangeGenerator.TILE_ENTITIES.get(TileInviteGenerator.class);
    }

    @Override
    protected BasePacketInvitations<TileInviteGenerator> withInvitations(InvitationMap invitations) {
        return new PacketInviteInvitations(allWorld, skipAccepted, skipSelf, onlyAccepted, onlySelf, world, invitations);
    }
}
