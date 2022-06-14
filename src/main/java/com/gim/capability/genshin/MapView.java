package com.gim.capability.genshin;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MapView<K, V> implements Map<K, V> {

    private final Map<K, V> inner;
    private final Collection<K> keys;

    public MapView(Map<K, V> inner, Collection<K> keys) {
        this.inner = inner;
        this.keys = keys;
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(keys);
    }

    @Override
    public Collection<V> values() {
        return inner.entrySet().stream().filter(x -> keys.contains(x.getKey())).map(Entry::getValue).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> result = inner.entrySet().stream().filter(x -> keys.contains(x.getKey())).collect(Collectors.toSet());
        return result;
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty() || inner.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key) && inner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Entry<K, V> entry : inner.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return keys.contains(entry.getKey());
            }
        }

        return false;
    }

    @Override
    public V get(Object key) {
        return keys.contains(key)
                ? inner.get(key)
                : null;
    }

    @Override
    public V put(K key, V value) {
        return keys.contains(key)
                ? inner.put(key, value)
                : null;
    }

    @Override
    public V remove(Object key) {
        return keys.contains(key)
                ? inner.remove(key)
                : null;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        for (K key : keys) {
            inner.remove(key);
        }

        keys.clear();
    }
}
