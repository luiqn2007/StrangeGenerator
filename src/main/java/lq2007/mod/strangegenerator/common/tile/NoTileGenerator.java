package lq2007.mod.strangegenerator.common.tile;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.BiConsumer;

public class NoTileGenerator extends BaseTileGenerator {

    public NoTileGenerator(TileEntityType<?> tileEntityTypeIn, boolean lockEnergy) {
        super(tileEntityTypeIn, lockEnergy);
    }

    @Override
    protected void updateCache(BiConsumer<Set<BaseTileGenerator>, BaseTileGenerator> update) { }

    @Override
    public void validate() { }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }
}
