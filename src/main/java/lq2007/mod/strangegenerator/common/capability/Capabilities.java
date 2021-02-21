package lq2007.mod.strangegenerator.common.capability;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class Capabilities {

    @CapabilityInject(IWorldData.class)
    public static Capability<IWorldData> CAP_WORLD_DATA = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IWorldData.class, IWorldData.STORAGE, WorldData::new);
    }

    public static IWorldData getWorldData(@Nullable World world) {
        if (world == null) return IWorldData.DUMMY;
        return world.getCapability(CAP_WORLD_DATA).orElse(IWorldData.DUMMY);
    }
}
