package lq2007.mod.strangegenerator.common.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class Player implements INBTSerializable<CompoundNBT> {

    protected ITextComponent name;
    protected UUID uuid;

    private ServerPlayerEntity player = null;

    public Player(UUID uuid) {
        this.uuid = uuid;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            player = null;
        } else {
            player = server.getPlayerList().getPlayerByUUID(uuid);
        }
        if (player == null) {
            this.name = null;
        } else {
            ITextComponent tabName = player.getTabListDisplayName();
            this.name = tabName == null ? player.getDisplayName() : tabName;
        }
    }

    public Player(UUID uuid, ITextComponent name) {
        this.uuid = uuid;
        this.name = name;
    }

    public Player(PlayerEntity player) {
        this.uuid = player.getUniqueID();
        if (player instanceof ServerPlayerEntity) {
            this.player = (ServerPlayerEntity) player;
        }
        setNameFromPlayer(player);
    }

    public Player(PacketBuffer buffer) {
        this.name = buffer.readTextComponent();
        this.uuid = null;
    }

    public Player(CompoundNBT nbt) {
        deserializeNBT(nbt);
        if (uuid == null) {
            throw new RuntimeException("No uuid!");
        }
    }

    public void write(PacketBuffer buffer) {
        buffer.writeTextComponent(name);
    }

    public ITextComponent getName() {
        if (name != null || updateName()) {
            return name;
        }
        return new StringTextComponent(uuid.toString());
    }

    public boolean updateName() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(uuid);
            if (player != null) {
                setNameFromPlayer(player);
                return true;
            }
        }
        return false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Optional<ServerPlayerEntity> getPlayer() {
        return Optional.ofNullable(getPlayerNullable());
    }

    @Nullable
    public ServerPlayerEntity getPlayerNullable() {
        if (player != null) {
            if (player.isAlive()) {
                return player;
            } else {
                player = null;
            }
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            player = server.getPlayerList().getPlayerByUUID(uuid);
            return player;
        }
        return null;
    }

    public CurrentStatus getStatus() {
        boolean isOnline = getPlayerNullable() != null;
        return new CurrentStatus(getName(), uuid, isOnline);
    }

    private void setNameFromPlayer(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ITextComponent tabName = ((ServerPlayerEntity) player).getTabListDisplayName();
            this.name = tabName == null ? player.getDisplayName() : tabName;
        } else {
            this.name = player.getDisplayName();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return uuid.equals(player.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUniqueId("uuid", uuid);
        if (name != null) {
            nbt.putString("name", ITextComponent.Serializer.toJson(name));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        uuid = nbt.getUniqueId("uuid");
        if (!updateName() && nbt.contains("name", Constants.NBT.TAG_STRING)) {
            name = ITextComponent.Serializer.getComponentFromJson(nbt.getString("name"));
        }
    }

    public static class CurrentStatus extends Player {

        private final boolean isOnline;

        public CurrentStatus(ITextComponent name, UUID uuid, boolean isOnline) {
            super(uuid, name);
            this.isOnline = isOnline;
        }

        public CurrentStatus(PacketBuffer buffer) {
            super(buffer);
            this.isOnline = buffer.readBoolean();
        }

        public boolean isOnline() {
            return isOnline;
        }

        @Override
        public void write(PacketBuffer buffer) {
            super.write(buffer);
            buffer.writeBoolean(isOnline);
        }
    }
}
