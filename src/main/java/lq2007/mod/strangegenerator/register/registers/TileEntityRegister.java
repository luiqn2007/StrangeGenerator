package lq2007.mod.strangegenerator.register.registers;

import lq2007.mod.strangegenerator.register.ObjectConstructor;
import lq2007.mod.strangegenerator.register.Register;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class TileEntityRegister extends BaseDeferredRegister<TileEntityType<?>, TileEntity> {

    private final Function<Class<? extends TileEntity>, Block[]> blockSupplier;

    public TileEntityRegister(Register context, String packageName, Function<Class<? extends TileEntity>, Block[]> blockSupplier) {
        super(ForgeRegistries.TILE_ENTITIES, context, TileEntity.class, packageName);
        this.blockSupplier = blockSupplier;
    }

    @Override
    protected Optional<ImmutablePair<Class<? extends TileEntity>, Supplier<TileEntityType<?>>>> build(String className) {
        try {
            Class<?> aClass = context.classLoader.loadClass(className);
            if (!aClass.isInterface() && resultType.isAssignableFrom(aClass)) {
                Class<? extends TileEntity> teType = (Class<? extends TileEntity>) aClass;
                if (!Modifier.isAbstract(teType.getModifiers())) {
                    Supplier<? extends TileEntity> supplier = new ObjectConstructor<>(teType);
                    return Optional.of(ImmutablePair.of(teType, () -> TileEntityType.Builder
                            .create(supplier, blockSupplier.apply(teType))
                            .build(null)));
                }
            }
            return Optional.empty();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("Skip " + className + " because of " + e.getMessage());
            return Optional.empty();
        }
    }
}
