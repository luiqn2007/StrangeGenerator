package lq2007.mod.strangegenerator.common.data;

import com.google.common.collect.*;
import lq2007.mod.strangegenerator.common.tile.TileInviteGenerator;
import lq2007.mod.strangegenerator.util.CollectionUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Map;
import java.util.UUID;

public class InvitationMap {

    public static InvitationMap EMPTY = new InvitationMap(ImmutableTable.of());

    private final ImmutableTable<ResourceLocation, BlockPos, ImmutableList<Player>> invitationMap;

    private InvitationMap(ImmutableTable<ResourceLocation, BlockPos, ImmutableList<Player>> invitationMap) {
        this.invitationMap = invitationMap;
    }

    public InvitationMap(PacketBuffer buffer) {
        int worldCount = buffer.readVarInt();
        ImmutableTable.Builder<ResourceLocation, BlockPos, ImmutableList<Player>> tableBuilder = new ImmutableTable.Builder<>();
        for (int i = 0; i < worldCount; i++) {
            ResourceLocation world = buffer.readResourceLocation();
            int posCount = buffer.readVarInt();
            for (int i1 = 0; i1 < posCount; i1++) {
                BlockPos pos = buffer.readBlockPos();
                int playerCount = buffer.readVarInt();
                ImmutableList.Builder<Player> listBuilder = new ImmutableList.Builder<>();
                for (int i2 = 0; i2 < playerCount; i2++) {
                    listBuilder.add(new Player(buffer));
                }
                tableBuilder.put(world, pos, listBuilder.build());
            }
        }
        this.invitationMap = tableBuilder.build();
    }

    public void forEachId(TriConsumer<ResourceLocation, BlockPos, Player> consumer) {
        invitationMap.rowMap().forEach((world, map) -> map.forEach((pos, uuids) -> uuids.forEach(uuid -> consumer.accept(world, pos, uuid))));
    }

    public void forEach(TriConsumer<ResourceLocation, BlockPos, ImmutableList<Player>> consumer) {
        invitationMap.rowMap().forEach((world, map) -> map.forEach((pos, uuids) -> consumer.accept(world, pos, uuids)));
    }

    public ImmutableList<Player> get(ResourceLocation world, BlockPos pos) {
        return invitationMap.get(world, pos);
    }

    public boolean contains(ResourceLocation world, BlockPos pos) {
        return invitationMap.contains(world, pos);
    }

    public void write(PacketBuffer buffer) {
        ImmutableMap<ResourceLocation, Map<BlockPos, ImmutableList<Player>>> worldMap = invitationMap.rowMap();
        buffer.writeVarInt(worldMap.size());
        worldMap.forEach((world, posMap) -> {
            buffer.writeResourceLocation(world);
            buffer.writeVarInt(posMap.size());
            posMap.forEach((pos, playerList) -> {
                buffer.writeBlockPos(pos);
                buffer.writeVarInt(playerList.size());
                playerList.forEach(player -> player.write(buffer));
            });
        });
    }

    public static class Builder {
        private final Table<ResourceLocation, BlockPos, ImmutableList.Builder<Player>> map;

        public Builder() {
            this.map = HashBasedTable.create();
        }

        public Builder add(ResourceLocation world, BlockPos pos, Player player) {
            CollectionUtils.computeIfAbsent(map, world, pos, ImmutableList.Builder::new).add(player);
            return this;
        }

        public Builder addAll(ResourceLocation world, BlockPos pos, Iterable<Player> players) {
            CollectionUtils.computeIfAbsent(map, world, pos, ImmutableList.Builder::new).addAll(players);
            return this;
        }

        public InvitationMap build() {
            ImmutableTable.Builder<ResourceLocation, BlockPos, ImmutableList<Player>> map2 = new ImmutableTable.Builder<>();
            map.rowMap().forEach((world, c) -> c.forEach((pos, builder) -> map2.put(world, pos, builder.build())));
            return new InvitationMap(map2.build());
        }
    }
}
