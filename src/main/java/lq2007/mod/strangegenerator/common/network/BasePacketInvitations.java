package lq2007.mod.strangegenerator.common.network;

import lq2007.mod.strangegenerator.common.data.InvitationMap;
import lq2007.mod.strangegenerator.common.data.Player;
import lq2007.mod.strangegenerator.common.tile.BaseTileGenerator;
import lq2007.mod.strangegenerator.common.tile.IInviteGenerator;
import lq2007.mod.strangegenerator.util.ByteUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * This packet will be sent when a player want to get all invitations.
 */
public abstract class BasePacketInvitations<T extends BaseTileGenerator & IInviteGenerator> extends BasePacket implements IPacketFromClient, IPacketFromServer {

    protected final boolean allWorld, skipAccepted, skipSelf, onlyAccepted, onlySelf;
    protected final ResourceLocation world;
    protected final InvitationMap invitations;

    /**
     * @param allWorld get invitations in all worlds.
     * @param skipAccepted True if skip generators the sender accepted their invitations
     * @param skipSelf True if skip generators created by the sender
     * @param onlyAccepted True if only get generators the sender accepted their invitations
     * @param onlySelf True if only get generators created by sender
     * @param world if allWorld is false and want to get generators in other world, use it.
     *              Otherwise use null and will find in sender's world.
     */
    @OnlyIn(Dist.CLIENT)
    public BasePacketInvitations(boolean allWorld, boolean skipAccepted, boolean skipSelf, boolean onlyAccepted, boolean onlySelf, @Nullable World world) {
        this.allWorld = allWorld;
        this.skipAccepted = skipAccepted;
        this.skipSelf = skipSelf;
        this.onlyAccepted = onlyAccepted;
        this.onlySelf = onlySelf;
        this.world = world == null ? null : world.getDimensionKey().getLocation();
        this.invitations = InvitationMap.EMPTY;
    }

    /**
     * @param allWorld get invitations in all worlds.
     * @param skipAccepted True if skip generators the sender accepted their invitations
     * @param skipSelf True if skip generators created by the sender
     * @param onlyAccepted True if only get generators the sender accepted their invitations
     * @param onlySelf True if only get generators created by sender
     * @param world if allWorld is false and want to get generators in other world, use it.
     *              Otherwise use null and will find in sender's world.
     */
    public BasePacketInvitations(boolean allWorld, boolean skipAccepted, boolean skipSelf, boolean onlyAccepted, boolean onlySelf, @Nullable ResourceLocation world, InvitationMap invitations) {
        this.allWorld = allWorld;
        this.skipAccepted = skipAccepted;
        this.skipSelf = skipSelf;
        this.onlyAccepted = onlyAccepted;
        this.onlySelf = onlySelf;
        this.world = world;
        this.invitations = invitations;
    }

    public BasePacketInvitations(PacketBuffer buffer) {
        boolean[] values = ByteUtils.unpack(buffer.readByte(), 6);
        this.allWorld = values[0];
        this.skipAccepted = values[1];
        this.skipSelf = values[2];
        this.onlyAccepted = values[3];
        this.onlySelf = values[4];
        this.world = values[5] ? buffer.readResourceLocation() : null;
        this.invitations = new InvitationMap(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        boolean hasWorld = world != null;
        byte filter = ByteUtils.pack(allWorld, skipAccepted, skipSelf, onlyAccepted, onlySelf, hasWorld);
        buffer.writeByte(filter);
        if (hasWorld) buffer.writeResourceLocation(world);
        invitations.write(buffer);
    }

    @Override
    public void consume(NetworkEvent.Context context, MinecraftServer server, ServerPlayerEntity sender) {
        withInvitations(fillInvitations(server, sender)).sendTo(sender);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void consume(NetworkEvent.Context context, net.minecraft.client.Minecraft minecraft, net.minecraft.client.entity.player.ClientPlayerEntity player) {
        // todo display
    }

    private InvitationMap fillInvitations(MinecraftServer server, ServerPlayerEntity sender) {
        if (onlyAccepted && skipAccepted) return InvitationMap.EMPTY;
        if (onlySelf && (skipSelf || skipAccepted)) return InvitationMap.EMPTY;
        TileEntityType<T> type = getGeneratorType();
        InvitationMap.Builder builder = new InvitationMap.Builder();
        Stream<T> stream;
        if (skipAccepted) {
            UUID self = sender.getUniqueID();
            if (allWorld) {
                stream = BaseTileGenerator.getGenerators(type).stream().filter(generator -> !generator.isInvited(self));
            } else {
                ServerWorld world = getWorld(server, sender);
                if (world == null) return InvitationMap.EMPTY;
                stream = BaseTileGenerator.getGenerators(type, world).stream().filter(generator -> !generator.isInvited(self));
            }
        } else if (skipSelf) {
            UUID self = sender.getUniqueID();
            if (allWorld) {
                stream = BaseTileGenerator.getGenerators(type).stream().filter(generator -> !generator.isOwner(self));
            } else {
                ServerWorld world = getWorld(server, sender);
                if (world == null) return InvitationMap.EMPTY;
                stream = BaseTileGenerator.getGenerators(type, world).stream().filter(generator -> !generator.isOwner(self));
            }
        } else if (onlyAccepted) {
            UUID self = sender.getUniqueID();
            if (allWorld) {
                stream = BaseTileGenerator.getGenerators(type).stream().filter(generator -> generator.isInvited(self));
            } else {
                ServerWorld world = getWorld(server, sender);
                if (world == null) return InvitationMap.EMPTY;
                stream = BaseTileGenerator.getGenerators(type, world).stream().filter(generator -> generator.isInvited(self));
            }
        } else if (onlySelf) {
            UUID self = sender.getUniqueID();
            if (allWorld) {
                stream = BaseTileGenerator.getGenerators(type, self).stream();
            } else {
                ServerWorld world = getWorld(server, sender);
                if (world == null) return InvitationMap.EMPTY;
                stream = BaseTileGenerator.getGenerators(type, world, self).stream();
            }
        } else {
            // no filter
            if (allWorld) {
                stream = BaseTileGenerator.getGenerators(type).stream();
            } else {
                ServerWorld world = getWorld(server, sender);
                if (world == null) return InvitationMap.EMPTY;
                stream = BaseTileGenerator.getGenerators(type, world).stream();
            }
        }
        stream.forEach(generator -> addGenerator(generator, builder));
        return builder.build();
    }

    private void addGenerator(T generator, InvitationMap.Builder builder) {
        ResourceLocation world = Objects.requireNonNull(generator.getWorld()).getDimensionKey().getLocation();
        BlockPos pos = generator.getPos();
        for (Player player : generator.getInvitedPlayers()) {
            builder.add(world, pos, player.getStatus());
        }
    }

    private ServerWorld getWorld(MinecraftServer server, ServerPlayerEntity player) {
        return world != null ? server.getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, world)) : player.getServerWorld();
    }

    protected abstract TileEntityType<T> getGeneratorType();

    protected abstract BasePacketInvitations<T> withInvitations(InvitationMap invitations);
}
