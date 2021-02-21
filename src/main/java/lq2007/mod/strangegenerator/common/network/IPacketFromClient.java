package lq2007.mod.strangegenerator.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * A packet send from client to server
 */
public interface IPacketFromClient {

    /**
     * Invoke when server received the packet.
     * @param context context
     * @param server server
     * @param sender player send the packet.
     */
    void consume(NetworkEvent.Context context, MinecraftServer server, ServerPlayerEntity sender);
}
