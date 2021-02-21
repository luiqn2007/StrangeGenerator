package lq2007.mod.strangegenerator.common.handler;

import lq2007.mod.strangegenerator.common.capability.Capabilities;
import lq2007.mod.strangegenerator.common.capability.IWorldData;
import lq2007.mod.strangegenerator.common.capability.WorldData;
import lq2007.mod.strangegenerator.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static lq2007.mod.strangegenerator.StrangeGenerator.ID;

@Mod.EventBusSubscriber
public class CapabilityEventHandler {

    private static final ResourceLocation WORLD_DATA = new ResourceLocation(ID, "cap_world_data");

    @SubscribeEvent
    public static void onAttachToWorld(AttachCapabilitiesEvent<World> event) {
        event.addCapability(WORLD_DATA, new WorldData());
    }

    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event) {
        IWorld world = event.getWorld();
        if (world instanceof ServerWorld) {
            try {
                ServerWorld sw = (ServerWorld) world;
                CompoundNBT data = Capabilities.getWorldData(sw).serializeNBT();
                Path path = sw.getServer().func_240776_a_(new FolderName("generator_data"));
                File file = path.resolve("generator_" + sw.getDimensionKey().getLocation().toString().replace(":", "=")).toFile();
                NBTUtils.save(data, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        if (world instanceof ServerWorld) {
            try {
                ServerWorld sw = (ServerWorld) world;
                IWorldData data = Capabilities.getWorldData(sw);
                Path path = sw.getServer().func_240776_a_(new FolderName("generator_data"));
                File file = path.resolve("generator_" + sw.getDimensionKey().getLocation().toString().replace(":", "=")).toFile();
                data.deserializeNBT(NBTUtils.read(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
