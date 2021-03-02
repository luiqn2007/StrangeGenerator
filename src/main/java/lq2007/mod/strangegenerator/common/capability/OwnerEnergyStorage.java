package lq2007.mod.strangegenerator.common.capability;

import lq2007.mod.strangegenerator.common.tile.BaseTileGenerator;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public class OwnerEnergyStorage extends EnergyStorage implements INBTSerializable<INBT> {

    private final BaseTileGenerator generator;
    private boolean isLock;

    public OwnerEnergyStorage(int capacity, BaseTileGenerator generator, boolean isLock) {
        super(capacity);
        this.generator = generator;
        this.isLock = isLock;
    }

    @Override
    public boolean canExtract() {
        return (!isLock || generator.isOwnerExist()) && super.canExtract();
    }

    @Override
    public INBT serializeNBT() {
        return CapabilityEnergy.ENERGY.getStorage().writeNBT(CapabilityEnergy.ENERGY, this, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CapabilityEnergy.ENERGY.getStorage().readNBT(CapabilityEnergy.ENERGY, this, null, nbt);
    }

    public boolean isFull() {
        return energy >= capacity;
    }

    public void removeEnergy(int energy) {
        if (this.energy > energy) {
            this.energy -= energy;
        } else {
            this.energy = 0;
        }
    }
}
