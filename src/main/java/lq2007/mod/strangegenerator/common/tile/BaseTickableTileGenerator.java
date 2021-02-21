package lq2007.mod.strangegenerator.common.tile;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class BaseTickableTileGenerator extends BaseTileGenerator implements ITickableTileEntity {

    protected int tick = 0;
    // todo configuration
    protected int tickDelay = 20;

    public BaseTickableTileGenerator(TileEntityType<?> tileEntityTypeIn, boolean lockEnergy) {
        super(tileEntityTypeIn, lockEnergy);
    }

    @Override
    public void tick() {
        if (world != null) {
            if (tick > 0) {
                tick--;
            } else {
                if (pauseFull() && storage.isFull()) {
                    return;
                }
                tick = tickDelay;
                update(!world.isRemote);
            }
            markDirty();
        }
    }

    protected abstract void update(boolean isServer);

    // todo configuration
    protected boolean pauseFull() {
        return false;
    }
}
