package lq2007.mod.strangegenerator.common.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.EnergyStorage;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

public class WorldData implements IWorldData {

    private boolean isBurned = false;
    private final Map<BlockPos, PistonGenerator> pistonGenerators = new HashMap<>();

    @Override
    public boolean isBurned() {
        return isBurned;
    }

    @Override
    public void setBurned(boolean isBurned) {
        this.isBurned = isBurned;
    }

    @Override
    public void newPistonGenerator(BlockPos pos, @Nullable PlayerEntity owner) {
        if (owner == null && (!pistonGenerators.containsKey(pos) || pistonGenerators.get(pos).owner == null)) {
            pistonGenerators.put(pos, new PistonGenerator((UUID) null));
        } else if (owner != null) {
            pistonGenerators.put(pos, new PistonGenerator(owner.getUniqueID()));
        }
    }

    @Override
    public void addPistonGenerator(BlockPos pos, UUID owner, EnergyStorage storage) {
        pistonGenerators.put(pos, new PistonGenerator(owner, storage));
    }

    @Override
    public int getPistonGeneratorCount(PlayerEntity owner) {
        return (int) pistonGenerators.values().stream()
                .filter(generator -> owner.getUniqueID().equals(generator.owner))
                .count();
    }

    @Override
    public void removePistonGenerator(BlockPos pos) {
        pistonGenerators.remove(pos);
    }

    @Override
    public Optional<PistonGenerator> getPistonGenerator(BlockPos pos) {
        return Optional.ofNullable(pistonGenerators.get(pos));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("burned", isBurned);
        ListNBT list = new ListNBT();
        pistonGenerators.forEach((pos, generator) -> list.add(generator.write(pos)));
        nbt.put("pistonGenerators", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        isBurned = nbt.getBoolean("burned");
        ListNBT list = nbt.getList("pistonGenerators", TAG_COMPOUND);
        pistonGenerators.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT compound = list.getCompound(i);
            BlockPos pos = NBTUtil.readBlockPos(compound);
            PistonGenerator generator = new PistonGenerator(compound);
            pistonGenerators.put(pos, generator);
        }
    }
}
