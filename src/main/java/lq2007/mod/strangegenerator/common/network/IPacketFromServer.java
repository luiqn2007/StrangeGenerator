package lq2007.mod.strangegenerator.common.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * A packet send from server to client
 */
public interface IPacketFromServer {

    /**
     * Invoke the method when client received the packet.
     * @param context context
     * @param minecraft Minecraft object
     * @param player client player
     */
    @OnlyIn(Dist.CLIENT)
    void consume(NetworkEvent.Context context, net.minecraft.client.Minecraft minecraft, net.minecraft.client.entity.player.ClientPlayerEntity player);
}
