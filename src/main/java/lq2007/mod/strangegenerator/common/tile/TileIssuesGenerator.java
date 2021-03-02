package lq2007.mod.strangegenerator.common.tile;

import lq2007.mod.strangegenerator.StrangeGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import org.eclipse.egit.github.core.RepositoryIssue;

import javax.annotation.Nullable;

// todo not finished: maybe need login
public class TileIssuesGenerator extends BaseTickableTileGenerator {

    // todo Thread -> ThreadPool
    private Thread checking = null;
    private Repository repository = null;

    public TileIssuesGenerator() {
        super(StrangeGenerator.TILE_ENTITIES.get(TileIssuesGenerator.class), false);
    }

    @Override
    protected void update(boolean isServer) {
        if (isServer && repository != null && checking == null) {
            checking = new IssueThread(repository, false, this);
            checking.start();
        }
    }

    public void setRepository(@Nullable Repository repository) {
        if (repository == null) {
            this.repository = null;
            stopIssue();
        } else {
            stopIssue();
            checking = new IssueThread(repository, true, this);
            checking.start();
        }
    }

    public void stopIssue() {
        if (checking != null) {
            checking.interrupt();
            checking = null;
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        if (nbt.contains("rOwner", Constants.NBT.TAG_STRING)
                && nbt.contains("rName", Constants.NBT.TAG_STRING)) {
            repository = new Repository(nbt.getString("rOwner"), nbt.getString("rName"));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        if (repository != null) {
            compound.putString("rOwner", repository.owner);
            compound.putString("rName", repository.repository);
        }
        return compound;
    }

    public static class Repository {
        public final String owner, repository;

        public Repository(String owner, String repository) {
            this.owner = owner;
            this.repository = repository;
        }
    }

    static class IssueThread extends Thread {

        Repository repository;
        boolean check;
        TileIssuesGenerator generator;

        IssueThread(Repository repository, boolean check, TileIssuesGenerator generator) {
            this.repository = repository;
            this.check = check;
            this.generator = generator;
        }

        @Override
        public void run() {
            try {
                if (!generator.isRemoved()) {
                    if (check) check();
                    else issue();
                }
            } catch (InterruptedException ignored) { }
        }

        private void check() throws InterruptedException {
        }

        private void issue() throws InterruptedException {

        }
    }
}
