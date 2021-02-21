package lq2007.mod.strangegenerator.common.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Objects;
import java.util.UUID;

public class Player {
    public final int id;
    public final ITextComponent name;

    public Player(int id, ITextComponent name) {
        this.id = id;
        this.name = name;
    }

    public Player(UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity player;
        if (server == null) {
            player = null;
        } else {
            player = server.getPlayerList().getPlayerByUUID(uuid);
        }
        if (player == null) {
            this.id = -1;
            this.name = new StringTextComponent(uuid.toString());
        } else {
            this.id = player.getEntityId();
            ITextComponent tabName = player.getTabListDisplayName();
            this.name = tabName == null ? player.getDisplayName() : tabName;
        }
    }

    public Player(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ITextComponent tabName = ((ServerPlayerEntity) player).getTabListDisplayName();
            this.name = tabName == null ? player.getDisplayName() : tabName;
        } else {
            this.name = player.getDisplayName();
        }
        this.id = player.getEntityId();
    }

    public Player(PacketBuffer buffer) {
        this.id = buffer.readVarInt();
        this.name = buffer.readTextComponent();
    }

    public void write(PacketBuffer buffer) {
        buffer.writeVarInt(id);
        buffer.writeTextComponent(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id && name.equals(player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
