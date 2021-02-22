package lq2007.mod.strangegenerator.common.tile;

import com.google.common.collect.ImmutableSet;
import lq2007.mod.strangegenerator.common.data.Player;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface IInviteGenerator {

    boolean isInvited(UUID uuid);

    void invite(UUID uuid);

    void invite(PlayerEntity player);

    ImmutableSet<Player> getInvitedPlayers();

    ImmutableSet<UUID> getInvitedIds();

    default void readInvitedPlayersFromNbt(CompoundNBT nbt, String key, Map<UUID, Player> invitedPlayers) {
        ListNBT list = nbt.getList(key, Constants.NBT.TAG_COMPOUND);
        invitedPlayers.clear();
        for (int i = 0; i < list.size(); i++) {
            Player player = new Player(list.getCompound(i));
            invitedPlayers.put(player.getUuid(), player);
        }
    }

    default void writeInvitedPlayersToNbt(CompoundNBT nbt, String key, Map<UUID, Player> invitedPlayers) {
        ListNBT list = new ListNBT();
        invitedPlayers.values().stream().map(Player::serializeNBT).forEach(list::add);
        nbt.put(key, list);
    }
}
