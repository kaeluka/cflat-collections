package com.github.kaeluka.cflat;

import com.github.kaeluka.cflat.annotations.Cflat;
import com.github.kaeluka.cflat.storage.HashMapStorage;
import com.github.kaeluka.cflat.storage.Storage;
import com.github.kaeluka.cflat.util.Mutable;

import javax.annotation.Nonnull;
import java.util.*;

@SuppressWarnings("unchecked")
@Cflat("*(tuple)->|(key,value)")
public class MapFlat<K,V> extends AbstractMap<K,V> {
    public Storage<Object> data;

    public MapFlat() { this(new HashMapStorage<>()); }

    public MapFlat(final Storage<Object> storage) { this.data = storage; }

    public int size() {
        final Mutable<Integer> res = new Mutable<>(0);
        data.foreachNonNull(i -> res.x++);
        return res.x;
    }

    @Override
    public boolean containsKey(Object key) {
        return data.has(new MapFlatIdx().tuple_nth(hash(key)).key());
    }

    public boolean containsValue(Object value) {
        final Iterator<Integer> indices = data.nonNullIndices();
        while (indices.hasNext()) {
            final Object wat = data.get(indices.next());
            if (indices.hasNext()) {
                final Integer valIdx = indices.next();
                final Object curVal = data.get(valIdx);
                if (value.equals(curVal)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int hash(Object key) {
        return (key.hashCode() << 1) & 0x7FFFFFFF; // remove the negative sign, if present
    }

    public V get(Object key) {
        MapFlatIdx hash = new MapFlatIdx().tuple_nth(hash(key));
        Mutable<Object> v = new Mutable<>(null);
        get(key, hash, v);
        return (V) v.x;
    }

    private void get(final Object key, MapFlatIdx hash, final Mutable<Object> v) {
        Object curKey;
        while ((curKey = data.get2(hash.key(), v)) != null && !key.equals(curKey)) {
            hash.tuple();
        }
    }

    public V put(K key, V value) {
        assert value != null;
        Object curKey;
        MapFlatIdx hash = new MapFlatIdx().tuple_nth(hash(key));
        while ((curKey = data.get(hash.key())) != null && !key.equals(curKey)) {
            hash.tuple();
        }
        data = data.set2(hash.key(), key, value);
        return null;
    }

    @Override @Nonnull
    public Set<K> keySet() {
        final Set<K> keys = new HashSet<>();
        data.foreachNonNull(i -> {
            if (i % 2 == 0) {
                keys.add((K) data.get(i));
            }
        });
        return keys;
    }

    @Override @Nonnull
    public Set<Map.Entry<K,V>> entrySet() {
        final Set<Entry<K, V>> entries = new HashSet<>();
        data.foreachNonNull(i -> {
            if (i % 2 == 0) {
                final K k = (K) data.get(i);
                final V v = (V) data.get(i+1);
                entries.add(new Entry<K, V>() {
                    @Override
                    public K getKey() { return k; }

                    @Override
                    public V getValue() { return v; }

                    @Override
                    public V setValue(final V value) {
                        return put(k, value);
                    }
                });
            }
        });
        return entries;
    }

    @Override
    public void clear() { data.clearAll(); }
}
