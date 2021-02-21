package lq2007.mod.strangegenerator.common.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * This packet will be sent when a player want to display all players in invite generator
 */
public abstract class BasePacketInvitePlayers<T extends BaseTileGenerator & IInviteGenerator> extends BasePacket implements IPacketFromClient, IPacketFromServer {

    protected final byte filterMask;
    protected final BlockPos pos;
    protected final ResourceLocation world, teWorld;
    protected final boolean allWorld, skipAccepted, onlyAccepted;
    protected final ImmutableList<Player> players;

    /**
     * Request all players
     * @param allWorld find players in all worlds. False means only find in one world.
     * @param skipAccepted ignore players accepted by the generator.
     * @param onlyAccepted only find player's accepted by the generator.
     * @param world if allWorld is false and not use sender's world, use this, otherwise use null
     * @param teWorld generator's world
     * @param pos generator's pos
     */
    public BasePacketInvitePlayers(boolean allWorld, boolean skipAccepted, boolean onlyAccepted, @Nullable ResourceLocation world, ResourceLocation teWorld, BlockPos pos) {
        this.filterMask = ByteUtils.pack(allWorld, skipAccepted, onlyAccepted, world != null);
        this.allWorld = allWorld;
        this.skipAccepted = skipAccepted;
        this.onlyAccepted = onlyAccepted;
        this.pos = pos;
        this.world = world;
        this.teWorld = teWorld;
        this.players = ImmutableList.of();
    }

    /**
     * Request all players
     * @param allWorld find players in all worlds. False means only find in one world.
     * @param skipAccepted ignore players accepted by the generator.
     * @param onlyAccepted only find player's accepted by the generator.
     * @param world if allWorld is false and not use sender's world, use this, otherwise use null
     * @param teWorld generator's world
     * @param pos generator's pos
     */
    public BasePacketInvitePlayers(boolean allWorld, boolean skipAccepted, boolean onlyAccepted, @Nullable ResourceLocation world, ResourceLocation teWorld, BlockPos pos, ImmutableList<Player> players) {
        this.filterMask = ByteUtils.pack(allWorld, skipAccepted, onlyAccepted, world != null);
        this.allWorld = allWorld;
        this.skipAccepted = skipAccepted;
        this.onlyAccepted = onlyAccepted;
        this.pos = pos;
        this.world = world;
        this.teWorld = teWorld;
        this.players = players;
    }

    public BasePacketInvitePlayers(PacketBuffer buffer) {
        this.filterMask = (byte) (buffer.readByte() & 0b1111);
        boolean[] values = ByteUtils.unpack(filterMask, 4);
        this.allWorld = values[0];
        this.skipAccepted = values[1];
        this.onlyAccepted = values[2];
        this.world = values[3] ? buffer.readResourceLocation() : null;
        this.teWorld = buffer.readResourceLocation();
        this.pos = buffer.readBlockPos();
        ImmutableList.Builder<Player> listBuilder = new ImmutableList.Builder<>();
        int playerCount = buffer.readVarInt();
        for (int i = 0; i < playerCount; i++) {
            listBuilder.add(new Player(buffer));
        }
        this.players = listBuilder.build();
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeByte(filterMask);
        if (world != null) {
            buffer.writeResourceLocation(world);
        }
        buffer.writeResourceLocation(teWorld);
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(players.size());
        players.forEach(player -> player.write(buffer));
    }

    @Override
    public void consume(NetworkEvent.Context context, MinecraftServer server, ServerPlayerEntity sender) {
        ImmutableList.Builder<Player> builder = new ImmutableList.Builder<>();
        ServerWorld teSWorld = server.getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, teWorld));
        if (teSWorld != null) {
            T generator = getGeneratorType().getIfExists(teSWorld, pos);
            if (generator != null) {
                if (onlyAccepted) {
                    for (UUID uuid : generator.getInvitedIds()) {
                        builder.add(new Player(uuid));
                    }
                } else {
                    Set<UUID> skips = skipAccepted ? generator.getInvitedIds() : ImmutableSet.of();
                    if (allWorld) {
                        server.getWorlds().forEach(sw -> addPlayers(sw, skips, builder));
                    } else {
                        ServerWorld sw = world == null ? sender.getServerWorld()
                                : server.getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, this.world));
                        addPlayers(sw, skips, builder);
                    }
                }
            }
        }
        withInvitations(builder.build()).sendTo(sender);
    }

    @Override
    public void consume(NetworkEvent.Context context, net.minecraft.client.Minecraft minecraft, net.minecraft.client.entity.player.ClientPlayerEntity player) {
        // todo update gui
    }

    private void addPlayers(@Nullable ServerWorld sw, Set<UUID> skips, ImmutableList.Builder<Player> builder) {
        if (sw == null) return;
        for (ServerPlayerEntity player : sw.getPlayers()) {
            if (!skips.contains(player.getUniqueID())) {
                builder.add(new Player(player));
            }
        }
    }

    protected abstract TileEntityType<T> getGeneratorType();

    protected abstract BasePacketInvitePlayers<T> withInvitations(ImmutableList<Player> invitations);
}
