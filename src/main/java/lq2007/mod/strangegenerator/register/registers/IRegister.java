package lq2007.mod.strangegenerator.register.registers;

import org.objectweb.asm.Type;

import java.util.Set;

public interface IRegister {

    /**
     * Save this class to cache
     * @param classLoader ClassLoader
     * @param clazz class
     * @param parent parent
     * @param interfaces interfaces
     */
    void cache(ClassLoader classLoader, Type clazz, Type parent, Set<Type> interfaces);

    /**
     * Register cached elements
     */
    void apply();
}
