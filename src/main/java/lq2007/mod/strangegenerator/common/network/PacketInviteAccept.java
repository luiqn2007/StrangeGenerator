package lq2007.mod.strangegenerator.common.network;

import lq2007.mod.strangegenerator.common.tile.IInviteGenerator;
import lq2007.mod.strangegenerator.common.tile.TileEntities;
import lq2007.mod.strangegenerator.common.tile.TileInviteGenerator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;

/**
 * This packet will be sent when a player accept the invitation.
 */
public class PacketInviteAccept extends BasePacket implements IPacketFromClient {

    private final BlockPos pos;
    private final ResourceLocation world;

    public PacketInviteAccept(BlockPos pos, @Nullable ResourceLocation world) {
        this.pos = pos;
        this.world = world;
    }

    public PacketInviteAccept(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
        this.world = buffer.readBoolean() ? buffer.readResourceLocation() : null;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        if (world == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeResourceLocation(world);
        }
    }

    @Override
    public void consume(NetworkEvent.Context context, MinecraftServer server, ServerPlayerEntity sender) {
        World world;
        if (this.world == null) {
            world = sender.world;
        } else {
            world = server.getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, this.world));
        }
        if (world != null) {
            TileEntity generator = world.getTileEntity(pos);
            if (generator instanceof IInviteGenerator) {
                ((IInviteGenerator) generator).invite(sender.getUniqueID());
            }
        }
    }
}
