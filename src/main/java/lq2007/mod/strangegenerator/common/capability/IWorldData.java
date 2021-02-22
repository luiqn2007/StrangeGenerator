package lq2007.mod.strangegenerator.common.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public interface IWorldData extends ICapabilitySerializable<CompoundNBT> {

    boolean isBurned();

    void setBurned(boolean isBurned);

    void newPistonGenerator(BlockPos pos, @Nullable PlayerEntity owner);

    void addPistonGenerator(BlockPos pos, UUID owner, EnergyStorage storage);

    int getPistonGeneratorCount(PlayerEntity owner);

    void removePistonGenerator(BlockPos pos);

    Optional<PistonGenerator> getPistonGenerator(BlockPos pos);

    @Nonnull
    @Override
    default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return Capabilities.CAP_WORLD_DATA.orEmpty(cap, LazyOptional.of(() -> this));
    }

    IWorldData DUMMY = new IWorldData() {
        @Override
        public CompoundNBT serializeNBT() {
            return new CompoundNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) { }

        @Override
        public boolean isBurned() {
            return false;
        }

        @Override
        public void setBurned(boolean isBurned) { }

        @Override
        public void newPistonGenerator(BlockPos pos, PlayerEntity owner) { }

        @Override
        public void addPistonGenerator(BlockPos pos, UUID owner, EnergyStorage storage) { }

        @Override
        public int getPistonGeneratorCount(PlayerEntity owner) {
            return -1;
        }

        @Override
        public void removePistonGenerator(BlockPos pos) {
        }

        @Override
        public Optional<PistonGenerator> getPistonGenerator(BlockPos pos) {
            return Optional.empty();
        }
    };

    Capability.IStorage<IWorldData> STORAGE = new Capability.IStorage<IWorldData>() {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IWorldData> capability, IWorldData instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IWorldData> capability, IWorldData instance, Direction side, INBT nbt) {
            instance.deserializeNBT((CompoundNBT) nbt);
        }
    };

    class PistonGenerator {
        public final EnergyStorage storage;
        public final UUID owner;

        PistonGenerator(@Nullable UUID owner) {
            this.storage = new EnergyStorage(100000 /* todo configuration */);
            this.owner = owner;
        }

        PistonGenerator(UUID owner, EnergyStorage storage) {
            this.storage = storage;
            this.owner = owner;
        }

        PistonGenerator(CompoundNBT compound) {
            this.storage = new EnergyStorage(compound.getInt("capacity"));
            this.storage.receiveEnergy(compound.getInt("energy"), false);
            if (compound.hasUniqueId("owner")) {
                this.owner = compound.getUniqueId("owner");
            } else {
                this.owner = null;
            }
        }

        CompoundNBT write(BlockPos pos) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("capacity", storage.getMaxEnergyStored());
            nbt.putInt("energy", storage.getEnergyStored());
            nbt.put("pos", NBTUtil.writeBlockPos(pos));
            if (owner != null) {
                nbt.putUniqueId("owner", owner);
            }
            return nbt;
        }
    }
}
