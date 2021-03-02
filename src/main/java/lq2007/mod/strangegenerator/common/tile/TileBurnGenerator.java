package lq2007.mod.strangegenerator.common.tile;

import lq2007.mod.strangegenerator.StrangeGenerator;
import lq2007.mod.strangegenerator.common.capability.Capabilities;
import lq2007.mod.strangegenerator.common.capability.IWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TileBurnGenerator extends BaseTickableTileGenerator {

    public TileBurnGenerator() {
        super(StrangeGenerator.TILE_ENTITIES.get(TileBurnGenerator.class), false);
    }

    @Override
    protected void update(boolean isServer) {
        if (isServer) {
            if (burnItems()) return;
            if (burnBlocks()) return;
            if (burnFluids()) return;
            if (burnAnimals()) return;
            if (burnEntities()) return;
            if (burnBoss()) return;
            if (burnBlocks2()) return;
            if (burnTileEntities()) return;
            if (burnPlayers()) return;
            burnWorlds();
        }
    }

    private boolean burnItems() {
        assert world != null;

        ServerWorld sw = (ServerWorld) world;
        List<Entity> items = sw.getEntities(EntityType.ITEM, Entity::isAlive);
        if (items.isEmpty() && shouldBurnOtherWorld()) {
            for (ServerWorld w : ServerLifecycleHooks.getCurrentServer().getWorlds()) {
                if (w == world) continue;
                List<Entity> list = w.getEntities(EntityType.ITEM, Entity::isAlive);
                if (!list.isEmpty()) {
                    items = list;
                }
            }
        }

        if (!items.isEmpty()) {
            int maxCount = getMaxBurnItemStackCount();
            int burnCount = 0;
            for (Entity item : items) {
                burnCount++;
                item.remove();
                ItemStack stack = ((ItemEntity) item).getItem();
                if (canBurnItem(stack)) {
                    float damage = stack.isDamaged() ? (1 - ((float) stack.getDamage()) / stack.getMaxDamage()) : 1;
                    float energy = getBaseEnergyByRarity(stack.getItem().getRarity(stack)) * stack.getCount() * damage;
                    for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
                        energy += getEnchantmentEnergy(entry.getKey(), entry.getValue());
                    }
                    receiveEnergy((int) energy);
                }
                if (pauseFull() && storage.isFull()) {
                    return true;
                }
                if (maxCount > 0 && burnCount >= maxCount) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    private boolean burnBlocks() {
        assert world != null;
        ServerWorld sw = (ServerWorld) this.world;
        List<ImmutablePair<BlockState, BlockPos>> blocks = new ArrayList<>();
        Chunk chunk = sw.getChunkAt(pos);
        int count = 0;
        boolean finished = false;
        ChunkPos centerPos = chunk.getPos();
        int r = 0, rx = 0, rz = 0;
        while (count < getMaxChunkSearch()) {
            ChunkPos chunkPos = new ChunkPos(centerPos.x + rx, centerPos.z + rz);
            if (!world.getChunkProvider().isChunkLoaded(chunkPos)) continue;
            int xStart = chunkPos.getXStart();
            int xEnd = chunkPos.getXEnd();
            int zStart = chunkPos.getZStart();
            int zEnd = chunkPos.getZEnd();
            for (int x = xStart; x < xEnd; x++) {
                for (int z = zStart; z < zEnd; z++) {
                    for (int y = world.getHeight(Heightmap.Type.WORLD_SURFACE, x, z) - 1; y >= 0; y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(pos);
                        if (canBurnBlock(state, pos)) {
                            blocks.add(ImmutablePair.of(state, pos));
                            if (blocks.size() >= getMaxBlockBurn()) {
                                finished = true;
                                break;
                            }
                        }
                    }
                    if (finished) break;
                }
                if (finished) break;
            }
            if (finished) break;
            if (rx == r && rz == r) {
                r++;
                rx = -r;
                rz = -r;
            } else if (rz == -r || rz == r) {
                if (rx == r) {
                    rz++;
                    rx = -r;
                } else {
                    rx++;
                }
            } else if (rx == r) {
                rz++;
                rx = -r;
            } else if (rx == -r) {
                rx = r;
            }
            count++;
        }
        if (blocks.isEmpty()) {
            return false;
        }
        for (ImmutablePair<BlockState, BlockPos> block : blocks) {
            world.removeBlock(block.right, false);
            receiveEnergy(getBlockBurnEnergy(block.left, block.right));
            if (pauseFull() && storage.isFull()) {
                return true;
            }
        }
        return true;
    }

    private boolean burnFluids() {
        assert world != null;
        ServerWorld sw = (ServerWorld) this.world;
        List<ImmutablePair<FluidState, BlockPos>> fluids = new ArrayList<>();
        Chunk chunk = sw.getChunkAt(pos);
        int count = 0;
        boolean finished = false;
        ChunkPos centerPos = chunk.getPos();
        int r = 0, rx = 0, rz = 0;
        while (count < getMaxChunkSearchFluid()) {
            ChunkPos chunkPos = new ChunkPos(centerPos.x + rx, centerPos.z + rz);
            if (!world.getChunkProvider().isChunkLoaded(chunkPos)) continue;
            int xStart = chunkPos.getXStart();
            int xEnd = chunkPos.getXEnd();
            int zStart = chunkPos.getZStart();
            int zEnd = chunkPos.getZEnd();
            for (int x = xStart; x < xEnd; x++) {
                for (int z = zStart; z < zEnd; z++) {
                    for (int y = world.getHeight(Heightmap.Type.WORLD_SURFACE, x, z) - 1; y >= 0; y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        FluidState state = world.getFluidState(pos);
                        if (canBurnFluid(state, pos)) {
                            fluids.add(ImmutablePair.of(state, pos));
                            if (fluids.size() >= getMaxFluidBurn()) {
                                finished = true;
                                break;
                            }
                        }
                    }
                    if (finished) break;
                }
                if (finished) break;
            }
            if (finished) break;
            if (rx == r && rz == r) {
                r++;
                rx = -r;
                rz = -r;
            } else if (rz == -r || rz == r) {
                if (rx == r) {
                    rz++;
                    rx = -r;
                } else {
                    rx++;
                }
            } else if (rx == r) {
                rz++;
                rx = -r;
            } else if (rx == -r) {
                rx = r;
            }
            count++;
        }
        if (fluids.isEmpty()) {
            return false;
        }
        for (ImmutablePair<FluidState, BlockPos> fluid : fluids) {
            world.removeBlock(fluid.right, false);
            receiveEnergy(getFluidBurnEnergy(fluid.left, fluid.right));
            if (pauseFull() && storage.isFull()) {
                return true;
            }
        }
        return true;
    }

    private boolean burnAnimals() {
        assert world != null;
        ServerWorld sw = (ServerWorld) world;
        List<AnimalEntity> animals = sw.getEntities()
                .filter(entity -> entity instanceof AnimalEntity)
                .map(entity -> (AnimalEntity) entity)
                .filter(this::canAnimalBurn)
                .collect(Collectors.toList());
        if (animals.isEmpty()) return false;
        for (AnimalEntity animal : animals) {
            animal.remove();
            float health = animal.getHealth();
            receiveEnergy((int) (health * getBaseAnimalEnergy(animal)));
            if (pauseFull() && storage.isFull()) {
                return true;
            }
        }
        return true;
    }

    private boolean burnEntities() {
        assert world != null;
        ServerWorld sw = (ServerWorld) world;
        List<Entity> entities = sw.getEntities()
                .filter(this::canEntityBurn)
                .collect(Collectors.toList());
        if (entities.isEmpty()) return false;
        for (Entity entity : entities) {
            float health;
            if (entity instanceof LivingEntity) {
                health = ((LivingEntity) entity).getHealth();
            } else {
                health = 1;
            }
            entity.remove();
            receiveEnergy((int) (health * getBaseEntityEnergy(entity)));
            if (pauseFull() && storage.isFull()) {
                return true;
            }
        }
        return true;
    }

    private boolean burnBoss() {
        assert world != null;
        ServerWorld sw = (ServerWorld) world;
        List<Entity> entities = sw.getEntities()
                .filter(entity -> !entity.isNonBoss())
                .filter(this::canBossBurn)
                .collect(Collectors.toList());
        if (entities.isEmpty()) return false;
        for (Entity entity : entities) {
            float health;
            if (entity instanceof LivingEntity) {
                health = ((LivingEntity) entity).getHealth();
            } else {
                health = 1;
            }
            entity.remove();
            receiveEnergy((int) (health * getBaseBossEnergy(entity)));
            if (pauseFull() && storage.isFull()) {
                return true;
            }
        }
        return true;
    }

    private boolean burnBlocks2() {
        assert world != null;
        ServerWorld sw = (ServerWorld) this.world;
        List<ImmutablePair<BlockState, BlockPos>> blocks = new ArrayList<>();
        Chunk chunk = sw.getChunkAt(pos);
        int count = 0;
        boolean finished = false;
        ChunkPos centerPos = chunk.getPos();
        int r = 0, rx = 0, rz = 0;
        while (count < getMaxChunkSearch2()) {
            ChunkPos chunkPos = new ChunkPos(centerPos.x + rx, centerPos.z + rz);
            if (!world.getChunkProvider().isChunkLoaded(chunkPos)) continue;
            int xStart = chunkPos.getXStart();
            int xEnd = chunkPos.getXEnd();
            int zStart = chunkPos.getZStart();
            int zEnd = chunkPos.getZEnd();
            for (int x = xStart; x < xEnd; x++) {
                for (int z = zStart; z < zEnd; z++) {
                    for (int y = world.getHeight(Heightmap.Type.WORLD_SURFACE, x, z) - 1; y >= 0; y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(pos);
                        if (canBurnBlock2(state, pos)) {
                            blocks.add(ImmutablePair.of(state, pos));
                            if (blocks.size() >= getMaxBlockBurn2()) {
                                finished = true;
                                break;
                            }
                        }
                    }
                    if (finished) break;
                }
                if (finished) break;
            }
            if (finished) break;
            if (rx == r && rz == r) {
                r++;
                rx = -r;
                rz = -r;
            } else if (rz == -r || rz == r) {
                if (rx == r) {
                    rz++;
                    rx = -r;
                } else {
                    rx++;
                }
            } else if (rx == r) {
                rz++;
                rx = -r;
            } else if (rx == -r) {
                rx = r;
            }
            count++;
        }
        if (blocks.isEmpty()) {
            return false;
        }
        for (ImmutablePair<BlockState, BlockPos> block : blocks) {
            world.removeBlock(block.right, false);
            receiveEnergy(getBlockBurnEnergy2(block.left, block.right));
            if (pauseFull() && storage.isFull()) {
                return true;
            }
        }
        return true;
    }

    private boolean burnTileEntities() {
        assert world != null;
        ServerWorld sw = (ServerWorld) this.world;
        List<ImmutablePair<BlockState, BlockPos>> blocks = new ArrayList<>();
        Chunk chunk = sw.getChunkAt(pos);
        int count = 0;
        boolean finished = false;
        ChunkPos centerPos = chunk.getPos();
        int r = 0, rx = 0, rz = 0;
        while (count < getMaxChunkSearchTile()) {
            ChunkPos chunkPos = new ChunkPos(centerPos.x + rx, centerPos.z + rz);
            if (!world.getChunkProvider().isChunkLoaded(chunkPos)) continue;
            int xStart = chunkPos.getXStart();
            int xEnd = chunkPos.getXEnd();
            int zStart = chunkPos.getZStart();
            int zEnd = chunkPos.getZEnd();
            for (int x = xStart; x < xEnd; x++) {
                for (int z = zStart; z < zEnd; z++) {
                    for (int y = world.getHeight(Heightmap.Type.WORLD_SURFACE, x, z) - 1; y >= 0; y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(pos);
                        if (canBurnBlockTile(state, pos)) {
                            blocks.add(ImmutablePair.of(state, pos));
                            if (blocks.size() >= getMaxBlockBurnTile()) {
                                finished = true;
                                break;
                            }
                        }
                    }
                    if (finished) break;
                }
                if (finished) break;
            }
            if (finished) break;
            if (rx == r && rz == r) {
                r++;
                rx = -r;
                rz = -r;
            } else if (rz == -r || rz == r) {
                if (rx == r) {
                    rz++;
                    rx = -r;
                } else {
                    rx++;
                }
            } else if (rx == r) {
                rz++;
                rx = -r;
            } else if (rx == -r) {
                rx = r;
            }
            count++;
        }
        if (blocks.isEmpty()) {
            return false;
        }
        for (ImmutablePair<BlockState, BlockPos> block : blocks) {
            world.removeBlock(block.right, false);
            world.removeTileEntity(block.right);
            receiveEnergy(getBlockBurnEnergyTile(block.left, block.right));
            if (pauseFull() && storage.isFull()) {
                return true;
            }
        }
        return true;
    }

    private boolean burnPlayers() {
        assert world != null;
        List<ServerPlayerEntity> players = ((ServerWorld) world).getPlayers(ServerPlayerEntity::isAlive);
        if (players.isEmpty()) return false;
        for (ServerPlayerEntity player : players) {
            if (canPlayerBurn(player)) {
                receiveEnergy((int) (player.getHealth() * getBasePlayerEnergy(player)));
                player.connection.disconnect(new StringTextComponent("You are burned!!!" /* todo to lang file */));

                if (pauseFull() && storage.isFull()) {
                    return true;
                }
            }
        }
        return true;
    }

    private void burnWorlds() {
        Capabilities.getWorldData(world).setBurned(true);
    }

    // todo configuration
    private boolean shouldBurnOtherWorld() {
        return true;
    }

    // todo configuration
    private boolean canBurnItem(ItemStack stack) {
        return true;
    }

    // todo configuration
    private float getBaseEnergyByRarity(Rarity rarity) {
        switch (rarity) {
            case COMMON: return 1;
            case UNCOMMON: return 1.5F;
            case RARE: return 2;
            case EPIC: return 2.5F;
            default: return 0;
        }
    }

    // todo configuration
    private float getEnchantmentEnergy(Enchantment enchantment, int level) {
        switch (enchantment.getRarity()) {
            case COMMON: return 1 * ((float) level) / enchantment.getMaxLevel();
            case UNCOMMON: return 2 * ((float) level) / enchantment.getMaxLevel();
            case RARE: return 5 * ((float) level) / enchantment.getMaxLevel();
            case VERY_RARE: return 10 * ((float) level) / enchantment.getMaxLevel();
            default: return 0;
        }
    }

    // todo configuration
    private int getMaxBurnItemStackCount() {
        return -1;
    }

    // todo configuration
    private int getMaxChunkSearch() {
        return 20;
    }

    // todo configuration
    private boolean canBurnBlock(BlockState state, BlockPos pos) {
        assert world != null;
        if (state.getFluidState().getFluid() != Fluids.EMPTY) return false;
        if (world.getTileEntity(pos) != null) return false;
        Block block = state.getBlock();
        if (block.isAir(state, world, pos)) return false;
        if (state.getMaterial().isFlammable()) {
            return true;
        }
        return state.getBlockHardness(world, pos) <= 2.0F /* COBBLESTONE */;
    }

    // todo configuration
    private int getMaxBlockBurn() {
        return 10;
    }

    // todo configuration
    private int getBlockBurnEnergy(BlockState state, BlockPos pos) {
        return 10;
    }

    // todo configuration
    private int getMaxChunkSearchFluid() {
        return 20;
    }

    // todo configuration
    private boolean canBurnFluid(FluidState state, BlockPos pos) {
        if (state.isEmpty()) return false;
        if (state.getFluid() == Fluids.EMPTY) return false;
        return state.isSource();
    }

    // todo configuration
    private int getMaxFluidBurn() {
        return 10;
    }

    // todo configuration
    private boolean canAnimalBurn(AnimalEntity animal) {
        return animal.isAlive();
    }

    // todo configuration
    private float getBaseAnimalEnergy(AnimalEntity animal) {
        return 1;
    }

    // todo configuration
    private boolean canEntityBurn(Entity entity) {
        return !(entity instanceof PlayerEntity) && entity.isNonBoss() && entity.isAlive();
    }

    // todo configuration
    private float getBaseEntityEnergy(Entity entity) {
        return 1;
    }

    // todo configuration
    private boolean canBossBurn(Entity entity) {
        return !(entity instanceof PlayerEntity) && entity.isAlive();
    }

    // todo configuration
    private float getBaseBossEnergy(Entity entity) {
        return 1;
    }

    // todo configuration
    private int getFluidBurnEnergy(FluidState state, BlockPos pos) {
        Fluid fluid = state.getFluid();
        switch (fluid.getAttributes().getRarity()) {
            case COMMON: return 100;
            case UNCOMMON: return 500;
            case RARE: return 1000;
            case EPIC: return 2000;
            default: return 0;
        }
    }

    // todo configuration
    private int getMaxChunkSearch2() {
        return 20;
    }

    // todo configuration
    private boolean canBurnBlock2(BlockState state, BlockPos pos) {
        assert world != null;
        if (state.getFluidState().getFluid() != Fluids.EMPTY) return false;
        if (world.getTileEntity(pos) != null) return false;
        Block block = state.getBlock();
        return !block.isAir(state, world, pos);
    }

    // todo configuration
    private int getMaxBlockBurn2() {
        return 10;
    }

    // todo configuration
    private int getBlockBurnEnergy2(BlockState state, BlockPos pos) {
        return 100;
    }

    // todo configuration
    private int getMaxChunkSearchTile() {
        return 20;
    }

    // todo configuration
    private boolean canBurnBlockTile(BlockState state, BlockPos pos) {
        assert world != null;
        TileEntity te = world.getTileEntity(pos);
        return te != null && !(te instanceof TileBurnGenerator);
    }

    // todo configuration
    private int getMaxBlockBurnTile() {
        return 10;
    }

    // todo configuration
    private int getBlockBurnEnergyTile(BlockState state, BlockPos pos) {
        return 10000;
    }

    private boolean canPlayerBurn(ServerPlayerEntity player) {
        return true;
    }

    private float getBasePlayerEnergy(ServerPlayerEntity player) {
        return 10;
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void onTickUpdate(TickEvent.ServerTickEvent event) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerWorld world : server.getWorlds()) {
                    IWorldData worldData = Capabilities.getWorldData(world);
                    if (worldData.isBurned()) {
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            player.connection.disconnect(new StringTextComponent("World is burned!!!" /* todo lang file */));
                        }
                        try {
                            world.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
