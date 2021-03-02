package lq2007.mod.strangegenerator.register.registers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lq2007.mod.strangegenerator.register.IAutoApply;
import lq2007.mod.strangegenerator.register.ObjectConstructor;
import lq2007.mod.strangegenerator.register.Register;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

public class BaseDeferredRegister<T extends IForgeRegistryEntry<T>, V> implements IRegister, Iterable<RegistryObject<T>>, IAutoApply {

    public final DeferredRegister<T> register;

    public final List<Class<? extends V>> type = new ArrayList<>();
    public final Class<V> resultType;
    public final Register context;
    public final String classPath;

    public final Map<Class<? extends V>, RegistryObject<T>> objMap = new HashMap<>();
    public final BiMap<Class<? extends V>, String> nameMap = HashBiMap.create();

    private final List<String> classNames = new ArrayList<>();

    public BaseDeferredRegister(IForgeRegistry<T> registry, Register context, Class<V> resultType, String classPath) {
        this.resultType = resultType;
        this.context = context;
        this.classPath = classPath;
        this.register = DeferredRegister.create(registry, context.modId);
        this.register.register(context.bus);
    }

    public BaseDeferredRegister(DeferredRegister<T> register, Class<V> resultType, Register context, String classPath) {
        this.register = register;
        this.resultType = resultType;
        this.context = context;
        this.classPath = classPath;
    }

    @Override
    public void cache(ClassLoader classLoader, Type clazz, Type parent, Set<Type> interfaces) {
        String className = clazz.getClassName();
        String packageName = className.substring(0, className.lastIndexOf("."));
        if (Objects.equals(packageName, classPath)) {
            classNames.add(className);
        }
    }

    @Override
    public void apply() {
        if (classNames.isEmpty()) return;
        classNames.stream().map(this::build).forEach(opt -> opt.ifPresent(pair -> {
            Class<? extends V> aClass = pair.left;
            Supplier<? extends T> supplier = pair.right;
            String name = aClass.getSimpleName().toLowerCase(Locale.ROOT);
            RegistryObject<T> registryObject = register.register(name, supplier);
            objMap.put(aClass, registryObject);
            nameMap.put(aClass, name);
            System.out.println("Registry " + aClass + " as " + registryObject.getId());
        }));
    }

    protected Optional<ImmutablePair<Class<? extends V>, Supplier<T>>> build(String className) {
        try {
            Class<?> aClass = context.classLoader.loadClass(className);
            if (!aClass.isInterface() && resultType.isAssignableFrom(aClass)) {
                if (!Modifier.isAbstract(aClass.getModifiers())) {
                    Class<? extends V> vType = (Class<? extends V>) aClass;
                    Class<? extends T> tType = (Class<? extends T>) aClass;
                    Supplier<T> supplier = new ObjectConstructor<>(tType);
                    return Optional.of(ImmutablePair.of(vType, supplier));
                }
            }
            return Optional.empty();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("Skip " + className + " because of " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Nonnull
    public Iterator<RegistryObject<T>> iterator() {
        return objMap.values().iterator();
    }

    public RegistryObject<T> getObj(Class<? extends V> aClass) {
        return objMap.get(aClass);
    }

    public T get(Class<? extends V> aClass, T defaultValue) {
        return objMap.containsKey(aClass) ? objMap.get(aClass).get() : defaultValue;
    }

    public <T2 extends T> T2 get(Class<? extends V> aClass) {
        return (T2) objMap.get(aClass).get();
    }
}
