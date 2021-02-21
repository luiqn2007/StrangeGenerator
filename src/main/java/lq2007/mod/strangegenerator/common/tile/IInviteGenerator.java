package lq2007.mod.strangegenerator.common.tile;

import com.google.common.collect.ImmutableSet;

import java.util.UUID;

public interface IInviteGenerator {

    boolean isInvited(UUID uuid);

    void invite(UUID uuid);

    ImmutableSet<UUID> getInvitedIds();
}
