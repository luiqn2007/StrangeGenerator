package lq2007.mod.strangegenerator.common.tile;

import lq2007.mod.strangegenerator.StrangeGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class TilePlayerCountGenerator extends BaseTickableTileGenerator {

    public TilePlayerCountGenerator() {
        super(StrangeGenerator.TILE_ENTITIES.get(TilePlayerCountGenerator.class), false);
    }

    // todo tps?
    @Override
    protected void update(boolean isServer) {
        if (isServer && !storage.isFull()) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            int count = server.getPlayerList().getCurrentPlayerCount();
            // todo configuration
            if (count > 200) {
                receiveEnergy(109 + (count - 200) / 20);
            } else if (count > 150) {
                receiveEnergy(104 + (count - 150) / 10);
            } else if (count > 80) {
                receiveEnergy(90 + (count - 80) / 5);
            } else if (count > 50) {
                receiveEnergy(80 + (count - 50) / 3);
            } else if (count > 10) {
                receiveEnergy(40 + count - 10);
            } else if (count > 5) {
                receiveEnergy(25 + (count - 5) * 3);
            } else {
                receiveEnergy(count * 5);
            }
        }
    }
}
