package lq2007.mod.strangegenerator.common.tile;

import lq2007.mod.strangegenerator.common.capability.Capabilities;
import lq2007.mod.strangegenerator.common.capability.IWorldData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TilePistonGenerator extends TileEntity {

    public TilePistonGenerator() {
        super(TileEntities.TILE_PISTON_GENERATOR.get());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (world != null && cap == CapabilityEnergy.ENERGY) {
            EnergyStorage storage = new EnergyStorage();
            IWorldData data = Capabilities.getWorldData(world);
            for (Direction direction : Direction.values()) {
                data.getPistonGenerator(pos.offset(direction)).map(g -> g.storage).ifPresent(storage::add);
            }
            if (!storage.isEmpty()) {
                return CapabilityEnergy.ENERGY.orEmpty(cap, LazyOptional.of(() -> storage));
            }
        }
        return super.getCapability(cap, side);
    }

    static class EnergyStorage implements IEnergyStorage {

        List<net.minecraftforge.energy.EnergyStorage> storages = new ArrayList<>();

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int e = 0;
            int re = maxExtract;
            for (net.minecraftforge.energy.EnergyStorage storage : storages) {
                int i = storage.extractEnergy(re, simulate);
                e += i;
                re -= i;
                if (re <= 0) break;
            }
            return e;
        }

        @Override
        public int getEnergyStored() {
            return storages.stream().mapToInt(net.minecraftforge.energy.EnergyStorage::getEnergyStored).sum();
        }

        @Override
        public int getMaxEnergyStored() {
            return storages.stream().mapToInt(net.minecraftforge.energy.EnergyStorage::getMaxEnergyStored).sum();
        }

        @Override
        public boolean canExtract() {
            return storages.stream().anyMatch(net.minecraftforge.energy.EnergyStorage::canExtract);
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        public boolean isEmpty() {
            return storages.isEmpty();
        }

        public void add(net.minecraftforge.energy.EnergyStorage storage) {
            storages.add(storage);
        }
    }
}
