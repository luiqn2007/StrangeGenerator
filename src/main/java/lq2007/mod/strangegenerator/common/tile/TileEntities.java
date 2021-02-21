package lq2007.mod.strangegenerator.common.tile;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import static lq2007.mod.strangegenerator.StrangeGenerator.ID;
import static lq2007.mod.strangegenerator.common.block.Blocks.*;

public class TileEntities {

    public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ID);

    public static RegistryObject<TileEntityType<TilePlayerCountGenerator>> TILE_PLAYER_COUNT_GENERATOR =
            REGISTER.register("generator_player_count", () -> create(TilePlayerCountGenerator::new, GENERATOR_PLAYER_COUNT));

    public static RegistryObject<TileEntityType<TileChatMessageGenerator>> TILE_CHAT_MESSAGE_GENERATOR =
            REGISTER.register("generator_chat_message", () -> create(TileChatMessageGenerator::new, GENERATOR_CHAT_MESSAGE));

    public static RegistryObject<TileEntityType<TileBurnGenerator>> TILE_BURN_GENERATOR =
            REGISTER.register("generator_burn", () -> create(TileBurnGenerator::new, GENERATOR_BURN));

    public static RegistryObject<TileEntityType<TileDeadGenerator>> TILE_DEAD_GENERATOR =
            REGISTER.register("generator_dead", () -> create(TileDeadGenerator::new, GENERATOR_DEAD));

    public static RegistryObject<TileEntityType<TileInviteGenerator>> TILE_INVITE_GENERATOR =
            REGISTER.register("generator_invite", () -> create(TileInviteGenerator::new, GENERATOR_INVITE));

    public static RegistryObject<TileEntityType<TileLoveGenerator>> TILE_LOVE_GENERATOR =
            REGISTER.register("generator_love", () -> create(TileLoveGenerator::new, GENERATOR_LOVE));

    public static RegistryObject<TileEntityType<TilePistonGenerator>> TILE_PISTON_GENERATOR =
            REGISTER.register("generator_piston", () -> create(TilePistonGenerator::new, GENERATOR_PISTON));

    public static <T extends TileEntity> TileEntityType<T> create(Supplier<T> creator, RegistryObject<? extends Block> block) {
        return TileEntityType.Builder.create(creator, block.get()).build(null);
    }
}
