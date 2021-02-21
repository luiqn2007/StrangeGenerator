package lq2007.mod.strangegenerator.common.block;

import lq2007.mod.strangegenerator.common.item.Items;
import lq2007.mod.strangegenerator.common.item.OwnerBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import static lq2007.mod.strangegenerator.StrangeGenerator.ID;

public class Blocks {

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);

    public static RegistryObject<PlayerCountGenerator> GENERATOR_PLAYER_COUNT =
            register("generator_player_count", PlayerCountGenerator::new);

    public static RegistryObject<ChatMessageGenerator> GENERATOR_CHAT_MESSAGE =
            register("generator_chat_message", ChatMessageGenerator::new);

    public static RegistryObject<BurnGenerator> GENERATOR_BURN =
            register("generator_burn", BurnGenerator::new);

    public static RegistryObject<DeadGenerator> GENERATOR_DEAD =
            register("generator_dead", DeadGenerator::new);

    public static RegistryObject<InviteGenerator> GENERATOR_INVITE =
            register("generator_invite", InviteGenerator::new);

    public static RegistryObject<LoveGenerator> GENERATOR_LOVE =
            register("generator_love", LoveGenerator::new);

    public static RegistryObject<PistonGenerator> GENERATOR_PISTON =
            register("generator_piston", PistonGenerator::new);

    public static RegistryObject<PistonGeneratorReader> READER_PISTON =
            registerNormal("generator_piston_reader", PistonGeneratorReader::new);

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> sup) {
        RegistryObject<T> obj = REGISTER.register(name, sup);
        Items.REGISTER.register(name, () -> new OwnerBlockItem<>(obj));
        return obj;
    }
    private static <T extends Block> RegistryObject<T> registerNormal(String name, Supplier<T> sup) {
        RegistryObject<T> obj = REGISTER.register(name, sup);
        Items.REGISTER.register(name, () -> new BlockItem(obj.get(), new Item.Properties().group(Items.GROUP_GENERATOR).maxStackSize(1)));
        return obj;
    }
}
