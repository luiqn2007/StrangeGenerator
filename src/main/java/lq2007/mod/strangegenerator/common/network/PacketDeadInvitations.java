package lq2007.mod.strangegenerator.common.network;

import lq2007.mod.strangegenerator.StrangeGenerator;
import lq2007.mod.strangegenerator.common.data.InvitationMap;
import lq2007.mod.strangegenerator.common.tile.TileDeadGenerator;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PacketDeadInvitations extends BasePacketInvitations<TileDeadGenerator> {

    public PacketDeadInvitations(boolean allWorld, boolean skipAccepted, boolean skipSelf, boolean onlyAccepted, boolean onlySelf, @Nullable World world) {
        super(allWorld, skipAccepted, skipSelf, onlyAccepted, onlySelf, world);
    }

    public PacketDeadInvitations(boolean allWorld, boolean skipAccepted, boolean skipSelf, boolean onlyAccepted, boolean onlySelf, @Nullable ResourceLocation world, InvitationMap invitations) {
        super(allWorld, skipAccepted, skipSelf, onlyAccepted, onlySelf, world, invitations);
    }

    public PacketDeadInvitations(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected TileEntityType<TileDeadGenerator> getGeneratorType() {
        return StrangeGenerator.TILE_ENTITIES.get(TileDeadGenerator.class);
    }

    @Override
    protected BasePacketInvitations<TileDeadGenerator> withInvitations(InvitationMap invitations) {
        return new PacketDeadInvitations(allWorld, skipAccepted, skipSelf, onlyAccepted, onlySelf, world, invitations);
    }
}
