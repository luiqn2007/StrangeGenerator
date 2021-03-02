package lq2007.mod.strangegenerator.common.network;

import com.google.common.collect.ImmutableSet;
import lq2007.mod.strangegenerator.register.registers.IRegister;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_SERVER;

public class NetworkRegister implements IRegister, Iterable<NetworkRegister.NetworkClass<? extends BasePacket>> {

    public int id = 0;

    private final List<Class<? extends BasePacket>> cachedClass = new ArrayList<>();
    public final List<NetworkClass<? extends BasePacket>> packets = new ArrayList<>();

    @Override
    public void cache(ClassLoader classLoader, Type clazz, Type parent, Set<Type> interfaces) {
        String className = clazz.getClassName();
        String packageName = className.substring(0, className.lastIndexOf("."));
        if ("lq2007.mod.strangegenerator.common.network".equals(packageName)) {
            try {
                Class<?> aClass = classLoader.loadClass(clazz.getClassName());
                if (!BasePacket.class.isAssignableFrom(aClass)) return;
                if (aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers())) return;
                aClass.getConstructor(PacketBuffer.class);
                cachedClass.add((Class<? extends BasePacket>) aClass);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) { }
        }
    }

    @Override
    public void apply() {
        Networks.initialize();
        for (Class<? extends BasePacket> packet : cachedClass) {
            System.out.println("Registry " + packet);
            NetworkDirection[] directions;
            if (IPacketFromClient.class.isAssignableFrom(packet)) {
                if (IPacketFromServer.class.isAssignableFrom(packet)) {
                    directions = new NetworkDirection[] {PLAY_TO_CLIENT, PLAY_TO_SERVER};
                } else {
                    directions = new NetworkDirection[] {PLAY_TO_SERVER};
                }
            } else if (IPacketFromServer.class.isAssignableFrom(packet)) {
                directions = new NetworkDirection[] {PLAY_TO_CLIENT};
            } else {
                Networks.LOGGER.warn("Packet {} should implement IClientPacket or IServerPacket.", packet);
                return;
            }
            try {
                NetworkClass<? extends BasePacket> aClass = new NetworkClass<>(id++, packet, directions);
                if (aClass.register()) {
                    packets.add(aClass);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Nonnull
    public Iterator<NetworkClass<? extends BasePacket>> iterator() {
        return packets.iterator();
    }

    public static class Encoder<T extends BasePacket> implements BiConsumer<T, PacketBuffer> {

        private final Method encoder;

        public Encoder(Class<? extends BasePacket> aClass) throws NoSuchMethodException {
            this.encoder = aClass.getMethod("encode", PacketBuffer.class);
            this.encoder.setAccessible(true);
        }

        @Override
        public void accept(BasePacket t, PacketBuffer buffer) {
            try {
                encoder.invoke(t, buffer);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Can't invoke this method.", e);
            }
        }
    }

    public static class Decoder<T extends BasePacket> implements Function<PacketBuffer, T> {

        private final Constructor<? extends BasePacket> constructor;

        public Decoder(Class<? extends BasePacket> aClass) throws NoSuchMethodException {
            this.constructor =  aClass.getConstructor(PacketBuffer.class);
            this.constructor.setAccessible(true);
        }

        @Override
        public T apply(PacketBuffer buffer) {
            try {
                return (T) constructor.newInstance(buffer);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Can't create a instance with constructor.", e);
            }
        }
    }

    public static class Consumer<T extends BasePacket> implements BiConsumer<T, Supplier<NetworkEvent.Context>> {

        private final Method consumer;

        public Consumer(Class<? extends BasePacket> aClass) throws NoSuchMethodException {
            this.consumer = aClass.getMethod("consume", Supplier.class);
            this.consumer.setAccessible(true);
        }

        @Override
        public void accept(T t, Supplier<NetworkEvent.Context> contextSupplier) {
            try {
                consumer.invoke(t, contextSupplier);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Can't invoke this method.", e);
            }
        }
    }

    public static class NetworkClass<T extends BasePacket> {
        public final int id;
        public final Class<T> type;
        public final ImmutableSet<NetworkDirection> directions;

        public final Encoder<T> encoder;
        public final Decoder<T> decoder;
        public final Consumer<T> consumer;

        public NetworkClass(int id, Class<T> type, NetworkDirection[] directions) throws NoSuchMethodException {
            this.id = id;
            this.type = type;
            this.directions = ImmutableSet.copyOf(directions);

            this.encoder = new Encoder<>(type);
            this.decoder = new Decoder<>(type);
            this.consumer = new Consumer<>(type);
        }

        boolean register() {
            Optional<NetworkDirection> direction;
            if (directions.contains(PLAY_TO_SERVER)) {
                if (directions.contains(PLAY_TO_CLIENT)) {
                    direction = Optional.empty();
                } else {
                    direction = Optional.of(PLAY_TO_SERVER);
                }
            } else if (directions.contains(PLAY_TO_CLIENT)) {
                direction = Optional.of(PLAY_TO_CLIENT);
            } else {
                return false;
            }
            Networks.CHANNEL.registerMessage(id, type, encoder, decoder, consumer, direction);
            return true;
        }
    }
}
