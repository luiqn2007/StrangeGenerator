package lq2007.mod.strangegenerator;

import lq2007.mod.strangegenerator.common.capability.Capabilities;
import lq2007.mod.strangegenerator.common.item.GroupGenerator;
import lq2007.mod.strangegenerator.common.network.NetworkRegister;
import lq2007.mod.strangegenerator.register.Register;
import lq2007.mod.strangegenerator.register.registers.BlockItemRegister;
import lq2007.mod.strangegenerator.register.registers.BlockRegister;
import lq2007.mod.strangegenerator.register.registers.ItemRegister;
import lq2007.mod.strangegenerator.register.registers.TileEntityRegister;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

/**
 * <p>Generator List</p>
 * <ul>
 *     <li>Can't keep up 发电机</li>
 *     <li>垃圾桶发电</li>
 *     <li>玩家位置发电</li>
 *     <li>红包发电</li>
 *     <li>传送吞物品/药水效果发电</li>
 *     <li>表情包发电（emojiful）</li>
 *     <li>生僻字发电机</li>
 *     <li>CrashReport 发电机</li>
 *     <li>异常发电机</li>
 *     <li>堆外内存发电机</li>
 *     <li>女仆数量发电机</li>
 * </ul>
 * <p>Skip</p>
 * <ul>
 *     <li>红石发电 ?????</li>
 *     <li>显卡发电 ?????</li>
 *     <li>Issues 发电</li>
 *     <li>汉化进度发电</li>
 * </ul>
 */
@Mod(StrangeGenerator.ID)
public class StrangeGenerator {

    public static final String ID = "strangegenerator";

    private static final Logger LOGGER = LogManager.getLogger();

    public static Register REGISTER;
    public static BlockRegister BLOCKS;
    public static ItemRegister ITEMS;
    public static TileEntityRegister TILE_ENTITIES;
    public static NetworkRegister NETWORKS;

    public StrangeGenerator() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);
        bus.addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);

        REGISTER = new Register();
        BLOCKS = REGISTER.add(new BlockRegister(REGISTER, "lq2007.mod.strangegenerator.common.block"));
        ITEMS = REGISTER.add(new ItemRegister(REGISTER, "lq2007.mod.strangegenerator.common.item"));
        TILE_ENTITIES = REGISTER.add(new TileEntityRegister(REGISTER, "lq2007.mod.strangegenerator.common.tile", te -> {
            String blockKey = te.getSimpleName().toLowerCase(Locale.ROOT).substring(4 /* "tile" */);
            Class<? extends Block> blockClass = BLOCKS.nameMap.inverse().get(blockKey);
            return new Block[] { BLOCKS.objMap.get(blockClass).get() };
        }));
        NETWORKS = REGISTER.add(new NetworkRegister());
        REGISTER.add(new BlockItemRegister(ITEMS.register, BLOCKS,
                b -> new Item.Properties().group(GroupGenerator.INSTANCE).maxStackSize(1)));
        REGISTER.execute();
    }

    private void setup(final FMLCommonSetupEvent event) {
        Capabilities.register();
        NETWORKS.apply();
        System.out.println("[StrangeGenerator]-------------------------------------------------------------");
        System.out.println("[StrangeGenerator]Register report: ");
        System.out.println("[StrangeGenerator]Blocks: ");
        BLOCKS.forEach(b -> System.out.println("[StrangeGenerator]\t" + b.getId() + ": " + b.get().getClass()));
        System.out.println("[StrangeGenerator]TileEntity: ");
        TILE_ENTITIES.forEach(te -> System.out.println("[StrangeGenerator]\t" + te.getId() + ": " + te.get()));
        System.out.println("[StrangeGenerator]Item: ");
        ITEMS.forEach(i -> System.out.println("[StrangeGenerator]\t" + i.getId() + ": " + i.get().getClass()));
        System.out.println("[StrangeGenerator]Networks: ");
        NETWORKS.forEach(network -> System.out.println("[StrangeGenerator]\t[" + network.id + "]" + network.directions + ": " + network.type));
        System.out.println("[StrangeGenerator]-------------------------------------------------------------");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
    }
}
