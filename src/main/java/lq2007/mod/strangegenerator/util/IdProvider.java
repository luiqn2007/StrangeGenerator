package lq2007.mod.strangegenerator.util;

import java.util.Collection;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class IdProvider implements IntSupplier, IntConsumer, IntPredicate {

    private int id = 0;
    private boolean full = false;
    private final Supplier<? extends Collection<? extends IHasId>> map;

    public IdProvider(Supplier<? extends Collection<? extends IHasId>> map) {
        this.map = map;
    }

    public int nextId() {
        if (full) {
            int s = id;
            id = nextIdIndex(s);
            if (isIdFree(s)) {
                return s;
            }
            while (id != s) {
                if (isIdFree(id)) {
                    return id;
                }
                id = nextIdIndex(s);
            }
            throw new RuntimeException("NO ID Empty!!!");
        }
        if (id == Integer.MAX_VALUE) {
            id = 0;
            full = true;
            return Integer.MAX_VALUE;
        }
        return id++;
    }

    public void setIdUsed(int id) {
        if (full) return;
        if (id == Integer.MAX_VALUE) {
            full = true;
            this.id = 0;
        } else {
            this.id = Math.max(id + 1, this.id);
        }
    }

    public boolean isIdFree(int id) {
        return map.get().stream().mapToInt(IHasId::getId).noneMatch(i -> i != id);
    }

    @Override
    public int getAsInt() {
        return nextId();
    }

    @Override
    public void accept(int value) {
        setIdUsed(value);
    }

    @Override
    public boolean test(int value) {
        return isIdFree(value);
    }

    private int nextIdIndex(int v) {
        if (v == Integer.MAX_VALUE) return 0;
        return v + 1;
    }

    public interface IHasId {
        int getId();
    }
}
