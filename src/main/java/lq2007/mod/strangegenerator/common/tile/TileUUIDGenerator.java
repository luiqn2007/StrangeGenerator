package lq2007.mod.strangegenerator.common.tile;

import lq2007.mod.strangegenerator.StrangeGenerator;

import java.util.UUID;

public class TileUUIDGenerator extends BaseTickableTileGenerator {

    public TileUUIDGenerator() {
        super(StrangeGenerator.TILE_ENTITIES.get(TileUUIDGenerator.class), false);
        tickDelay = 100; // 5s
    }

    @Override
    protected void update(boolean isServer) {
        if (isServer) {
            UUID rand = UUID.randomUUID();
            long mostSigBits = owner.getMostSignificantBits() + rand.getMostSignificantBits();
            long leastSigBits = owner.getLeastSignificantBits() + rand.getLeastSignificantBits();
            UUID check = new UUID(mostSigBits, leastSigBits);
            receiveEnergy(check.variant() % 100);
        }
    }
}
