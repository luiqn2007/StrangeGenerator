package lq2007.mod.strangegenerator.common.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Supplier;

public abstract class BasePacket {

    public BasePacket() { }

    public abstract void encode(PacketBuffer buffer);

    public final void consume(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (this instanceof IPacketFromServer && context.getDirection().getOriginationSide().isServer()) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                ((IPacketFromServer) this).consume(context, mc, mc.player);
            } else if (this instanceof IPacketFromClient && context.getDirection().getOriginationSide().isClient()) {
                ((IPacketFromClient) this).consume(context, ServerLifecycleHooks.getCurrentServer(), context.getSender());
            }
            context.setPacketHandled(true);
        });
    }

    /**
     * Send to players track the entity.
     * @param entity entity is tracked
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void sendToTracker(Entity entity, PlayerEntity... ignorePlayers) {
        if (!entity.world.isRemote) {
            sendToTracker(new ChunkPos(entity.chunkCoordX, entity.chunkCoordZ), (ServerWorld) entity.world, ignorePlayers);
        }
    }

    /**
     * Send to players track the position
     * @param pos pos
     * @param world world
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void sendToTracker(BlockPos pos, World world, PlayerEntity... ignorePlayers) {
        if (!world.isRemote) {
            sendToTracker(new ChunkPos(pos), (ServerWorld) world, ignorePlayers);
        }
    }

    /**
     * Send to players track the chunk
     * @param pos chunk pos
     * @param world world
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void sendToTracker(ChunkPos pos, ServerWorld world, PlayerEntity... ignorePlayers) {
        world.getChunkProvider().chunkManager.getTrackingPlayers(pos, false)
                .filter(player -> !ArrayUtils.contains(ignorePlayers, player))
                .forEach(this::sendTo);
    }

    /**
     * Send to all players in the server.
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void sendToAllPlayers(PlayerEntity... ignorePlayers) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.getPlayerList().getPlayers().stream()
                    .filter(player -> !ArrayUtils.contains(ignorePlayers, player))
                    .forEach(this::sendTo);
        }
    }

    /**
     * Send to all players in the world.
     * @param world world
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void sendToAllPlayers(World world, PlayerEntity... ignorePlayers) {
        if (!world.isRemote) {
            ((ServerWorld) world).getPlayers().stream()
                    .filter(player -> !ArrayUtils.contains(ignorePlayers, player))
                    .forEach(this::sendTo);
        }
    }

    /**
     * Send to all players in the world (dimension).
     * @param dimension dimension
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void sendToAllPlayers(RegistryKey<World> dimension, PlayerEntity... ignorePlayers) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerWorld world = server.getWorld(dimension);
            if (world != null) {
                sendToAllPlayers(world, ignorePlayers);
            }
        }
    }

    /**
     * Send to all players in the world (dimension).
     * @param dimension dimension
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void sendToAllPlayers(DimensionType dimension, PlayerEntity... ignorePlayers) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerWorld world : server.getWorlds()) {
                if (world.getDimensionType() == dimension) {
                    sendToAllPlayers(world, ignorePlayers);
                    break;
                }
            }
        }
    }

    /**
     * Send to client player.
     * @param player player
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void sendTo(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            Networks.CHANNEL.sendTo(this, ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_SERVER);
        }
    }

    /**
     * Send to server.
     */
    @OnlyIn(Dist.CLIENT)
    public void sendToServer() {
        Networks.CHANNEL.sendToServer(this);
    }
}
