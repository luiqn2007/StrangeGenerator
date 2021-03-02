package lq2007.mod.strangegenerator.common.tile;

import lq2007.mod.strangegenerator.StrangeGenerator;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class TileChatMessageGenerator extends BaseTileGenerator {

    public TileChatMessageGenerator() {
        super(StrangeGenerator.TILE_ENTITIES.get(TileChatMessageGenerator.class), false);
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {

        @SubscribeEvent
        public static void onChat(ServerChatEvent event) {
            int count;
            int length = event.getMessage().length();
            // todo configuration
            if (length > 100) {
                count = 66 + (length - 100) / 5;
            } else if (length > 50) {
                count = 50 + (length - 50) / 3;
            } else if (length > 10) {
                count = 30 + (length - 10) / 2;
            } else if (length > 5) {
                count = 25 + (length - 5);
            } else {
                count = length * 5;
            }

            TileEntityType<TileChatMessageGenerator> type = StrangeGenerator.TILE_ENTITIES.get(TileChatMessageGenerator.class);
            ServerWorld world = event.getPlayer().getServerWorld();
            UUID owner = event.getPlayer().getUniqueID();
            BaseTileGenerator.getGenerators(type, world, owner).forEach(te -> te.receiveEnergy(count));
        }
    }
}
