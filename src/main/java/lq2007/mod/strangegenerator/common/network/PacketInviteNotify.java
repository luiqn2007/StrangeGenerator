package lq2007.mod.strangegenerator.common.network;

import lq2007.mod.strangegenerator.common.data.Player;
import lq2007.mod.strangegenerator.common.tile.TileInviteGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;

/**
 * This packet will be sent when a player want to invite other player(s).
 */
public class PacketInviteNotify extends BasePacket implements IPacketFromServer {

    private final Player owner;
    private final BlockPos pos;
    private final ResourceLocation world;

    public PacketInviteNotify(TileInviteGenerator generator, PlayerEntity owner) {
        this.owner = new Player(owner);
        this.pos = generator.getPos();
        this.world = Objects.requireNonNull(generator.getWorld()).getDimensionKey().getLocation();
    }

    public PacketInviteNotify(PacketBuffer buffer) {
        this.owner = new Player(buffer);
        this.pos = buffer.readBlockPos();
        this.world = buffer.readResourceLocation();
    }

    @Override
    public void encode(PacketBuffer buffer) {
        owner.write(buffer);
        buffer.writeBlockPos(pos);
        buffer.writeResourceLocation(world);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void consume(NetworkEvent.Context context, net.minecraft.client.Minecraft minecraft, net.minecraft.client.entity.player.ClientPlayerEntity player) {
        // todo notify player a new invite received.
    }
}
