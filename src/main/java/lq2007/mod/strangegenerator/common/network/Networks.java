package lq2007.mod.strangegenerator.common.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

import static lq2007.mod.strangegenerator.StrangeGenerator.ID;

public class Networks {

    public static final ResourceLocation CHANNEL_KEY = new ResourceLocation(ID, "network");
    public static final Logger LOGGER = LogManager.getLogger();
    public static SimpleChannel CHANNEL;

    public static void initialize() {
        Predicate<String> alwaysTrue = s -> true;
        CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_KEY, () -> "1", alwaysTrue, alwaysTrue);
    }
}
