package lq2007.mod.strangegenerator.util;

import com.google.common.collect.Table;

import java.util.Map;
import java.util.function.Supplier;

public class CollectionUtils {

    public static <R, C, V> V computeIfAbsent(Table<R, C, V> table, R row, C column, Supplier<V> value) {
        if (table.contains(row, column)) {
            return table.get(row, column);
        } else {
            V v = value.get();
            table.put(row, column, v);
            return v;
        }
    }

    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Supplier<V> value) {
        return map.computeIfAbsent(key, k -> value.get());
    }
}
