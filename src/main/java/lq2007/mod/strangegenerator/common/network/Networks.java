package lq2007.mod.strangegenerator.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static lq2007.mod.strangegenerator.StrangeGenerator.ID;
import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_SERVER;

public class Networks {

    private static final ResourceLocation CHANNEL_KEY = new ResourceLocation(ID, "network");
    private static final Logger LOGGER = LogManager.getLogger();
    public static SimpleChannel CHANNEL;
    private static int id;

    public static void register() {
        Predicate<String> alwaysTrue = s -> true;
        CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_KEY, () -> "1", alwaysTrue, alwaysTrue);
        // IInviteGenerator
        register(PacketInviteAccept.class, PacketInviteAccept::new);
        register(PacketInviteNotify.class, PacketInviteNotify::new);
        // InviteGeneration
        register(PacketInviteInvitations.class, PacketInviteInvitations::new);
        register(PacketInvitePlayer.class, PacketInvitePlayer::new);
        // DeadGeneration
        register(PacketDeadInvitations.class, PacketDeadInvitations::new);
        register(PacketDeadPlayer.class, PacketDeadPlayer::new);
    }

    private static <T extends BasePacket> void register(Class<T> type, Function<PacketBuffer, T> decoder) {
        Optional<NetworkDirection> direction;
        if (IPacketFromClient.class.isAssignableFrom(type)) {
            if (IPacketFromServer.class.isAssignableFrom(type)) {
                direction = Optional.empty();
            } else {
                direction = Optional.of(PLAY_TO_SERVER);
            }
        } else if (IPacketFromServer.class.isAssignableFrom(type)) {
            direction = Optional.of(PLAY_TO_CLIENT);
        } else {
            LOGGER.warn("Packet {} should implement IClientPacket or IServerPacket.", type);
            return;
        }
        CHANNEL.registerMessage(id++, type, T::encode, decoder, T::consume, direction);
    }
}
