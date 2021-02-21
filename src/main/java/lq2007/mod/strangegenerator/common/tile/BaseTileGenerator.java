package lq2007.mod.strangegenerator.common.tile;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import lq2007.mod.strangegenerator.common.capability.OwnerEnergyStorage;
import lq2007.mod.strangegenerator.common.item.OwnerBlockItem;
import lq2007.mod.strangegenerator.util.CollectionUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class BaseTileGenerator extends TileEntity {

    protected static final Map<TileEntityType<?>, Set<BaseTileGenerator>> CACHE_TYPE = new HashMap<>();
    protected static final Map<TileEntityType<?>, Table<ServerWorld, UUID, Set<BaseTileGenerator>>> CACHE_WORLD_OWNER = new HashMap<>();
    protected static final Table<TileEntityType<?>, ServerWorld, Set<BaseTileGenerator>> CACHE_WORLD = HashBasedTable.create();
    protected static final Table<TileEntityType<?>, UUID, Set<BaseTileGenerator>> CACHE_OWNER = HashBasedTable.create();

    protected final OwnerEnergyStorage storage;
    protected UUID owner;

    public BaseTileGenerator(TileEntityType<?> tileEntityTypeIn, boolean lockEnergy) {
        super(tileEntityTypeIn);
        storage = new OwnerEnergyStorage(10000000, this, lockEnergy);
    }

    public void placedBy(World worldIn, BlockPos pos, BlockState state, PlayerEntity placer, ItemStack stack) {
        owner = placer.getUniqueID();
        markDirty();
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwnerExist() {
        if (world != null && !world.isRemote) {
            MinecraftServer server = world.getServer();
            if (server != null) {
                return server.getPlayerList().getPlayerByUUID(owner) != null;
            }
        }
        return false;
    }

    public boolean isOwner(PlayerEntity player) {
        return owner != null && owner.equals(player.getUniqueID());
    }

    public boolean isOwner(UUID player) {
        return owner != null && owner.equals(player);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        owner = nbt.getUniqueId("owner");
        storage.deserializeNBT(nbt.get("energy"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.putUniqueId("owner", owner);
        compound.put("energy", storage.serializeNBT());
        return compound;
    }

    public void receiveEnergy(int energy) {
        if (storage.receiveEnergy(energy, false) > 0) {
            markDirty();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        LazyOptional<T> optional = CapabilityEnergy.ENERGY.orEmpty(cap, LazyOptional.of(() -> storage));
        return optional.isPresent() ? optional : super.getCapability(cap, side);
    }

    @Override
    public void remove() {
        super.remove();
        updateCache(Set::remove);
    }

    @Override
    public void validate() {
        super.validate();
        updateCache(Set::add);
    }

    private void updateCache(BiConsumer<Set<BaseTileGenerator>, BaseTileGenerator> update) {
        Objects.requireNonNull(world, "World is NULL!!!");
        if (owner == null) {
            PlayerEntity placer = OwnerBlockItem.findPlacer(world, pos);
            Objects.requireNonNull(placer, "Owner is NULL!!!");
            owner = placer.getUniqueID();
        }
        if (world instanceof ServerWorld) {
            TileEntityType<?> type = getType();
            ServerWorld sw = (ServerWorld) this.world;
            // CACHE_WORLD_OWNER
            Table<ServerWorld, UUID, Set<BaseTileGenerator>> woTable = CollectionUtils.computeIfAbsent(CACHE_WORLD_OWNER, type, HashBasedTable::create);
            Set<BaseTileGenerator> woSet = CollectionUtils.computeIfAbsent(woTable, sw, owner, HashSet::new);
            update.accept(woSet, this);
            // CACHE_WORLD
            Set<BaseTileGenerator> cSet = CollectionUtils.computeIfAbsent(CACHE_WORLD, type, sw, HashSet::new);
            update.accept(cSet, this);
            // CACHE_OWNER
            Set<BaseTileGenerator> oSet = CollectionUtils.computeIfAbsent(CACHE_OWNER, type, owner, HashSet::new);
            update.accept(oSet, this);
            // CACHE_TYPE
            Set<BaseTileGenerator> tSet = CollectionUtils.computeIfAbsent(CACHE_TYPE, type, HashSet::new);
            update.accept(tSet, this);
        }
    }

    public static int getGeneratorCount(TileEntityType<?> type, ServerWorld world, UUID owner) {
        if (CACHE_WORLD_OWNER.containsKey(type)) {
            Table<ServerWorld, UUID, Set<BaseTileGenerator>> table = CACHE_WORLD_OWNER.get(type);
            if (table.contains(world, owner)) {
                return table.get(world, owner).size();
            }
        }
        return 0;
    }

    public static int getGeneratorCount(TileEntityType<?> type, ServerWorld world) {
        if (CACHE_WORLD.contains(type, world)) {
            return CACHE_WORLD.get(type, world).size();
        }
        return 0;
    }

    public static int getGeneratorCount(TileEntityType<?> type, UUID owner) {
        if (CACHE_OWNER.contains(type, owner)) {
            return CACHE_OWNER.get(type, owner).size();
        }
        return 0;
    }

    public static int getGeneratorCount(TileEntityType<?> type) {
        if (CACHE_TYPE.containsKey(type)) {
            return CACHE_TYPE.get(type).size();
        }
        return 0;
    }

    public static <T extends BaseTileGenerator> ImmutableSet<T> getGenerators(TileEntityType<T> type, @Nullable ServerWorld world, UUID owner) {
        if (world == null) return ImmutableSet.of();
        if (CACHE_WORLD_OWNER.containsKey(type)) {
            Table<ServerWorld, UUID, Set<BaseTileGenerator>> table = CACHE_WORLD_OWNER.get(type);
            if (table.contains(world, owner)) {
                Set<BaseTileGenerator> generators = table.get(world, owner);
                if (!generators.isEmpty()) {
                    return ImmutableSet.copyOf((Set<T>) generators);
                }
            }
        }
        return ImmutableSet.of();
    }

    public static <T extends BaseTileGenerator> ImmutableSet<T> getGenerators(TileEntityType<T> type, ServerWorld world) {
        if (CACHE_WORLD.contains(type, world)) {
            Set<BaseTileGenerator> generators = CACHE_WORLD.get(type, world);
            if (!generators.isEmpty()) {
                return ImmutableSet.copyOf((Set<T>) generators);
            }
        }
        return ImmutableSet.of();
    }

    public static <T extends BaseTileGenerator> ImmutableSet<T> getGenerators(TileEntityType<T> type, UUID owner) {
        if (CACHE_OWNER.contains(type, owner)) {
            Set<BaseTileGenerator> generators = CACHE_OWNER.get(type, owner);
            if (!generators.isEmpty()) {
                return ImmutableSet.copyOf((Set<T>) generators);
            }
        }
        return ImmutableSet.of();
    }

    public static <T extends BaseTileGenerator> ImmutableSet<T> getGenerators(TileEntityType<T> type) {
        if (CACHE_TYPE.containsKey(type)) {
            Set<BaseTileGenerator> generators = CACHE_TYPE.get(type);
            if (!generators.isEmpty()) {
                return ImmutableSet.copyOf((Set<T>) generators);
            }
        }
        return ImmutableSet.of();
    }
}
