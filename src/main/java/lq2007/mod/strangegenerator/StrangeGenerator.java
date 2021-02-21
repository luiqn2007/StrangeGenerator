package lq2007.mod.strangegenerator;

import lq2007.mod.strangegenerator.common.block.Blocks;
import lq2007.mod.strangegenerator.common.capability.Capabilities;
import lq2007.mod.strangegenerator.common.item.Items;
import lq2007.mod.strangegenerator.common.network.Networks;
import lq2007.mod.strangegenerator.common.tile.TileEntities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.WorldPersistenceHooks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>Generator List</p>
 * <ul>
 *     <li>Can't keep up 发电机</li>
 *     <li>垃圾桶发电</li>
 *     <li>活塞发电机</li>
 *     <li>建筑发电机</li>
 *     <li>红石发电（？？？）</li>
 *     <li>UUID 发电</li>
 *     <li>显卡发电</li>
 *     <li>玩家位置发电</li>
 *     <li>红包发电</li>
 *     <li>汉化进度发电</li>
 *     <li>Issues 发电</li>
 *     <li>传送吞物品/药水效果发电</li>
 *     <li>表情包发电（emojiful）</li>
 *     <li>生僻字发电机</li>
 *     <li>CrashReport 发电机</li>
 *     <li>异常发电机</li>
 *     <li>堆外内存发电机</li>
 *     <li>女仆数量发电机</li>
 * </ul>
 */
@Mod(StrangeGenerator.ID)
public class StrangeGenerator {

    public static final String ID = "strangegenerator";

    private static final Logger LOGGER = LogManager.getLogger();

    public StrangeGenerator() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);
        bus.addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);

        Blocks.REGISTER.register(bus);
        TileEntities.REGISTER.register(bus);
        Items.REGISTER.register(bus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        Capabilities.register();
        Networks.register();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
    }
}
