package com.gim.capability.genshin;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ObservableMap<K, V> extends AbstractMap<K, V> {

    private final Map<K, V> inner;
    private final Consumer<String> onChange;

    private Map<K, V> observable;
    private BiConsumer<String, Map<K, V>> callback;
    private boolean handle;

    /**
     * Called from core modification
     * Replacing LivingEntity active effects
     */
    public ObservableMap() {
        this.inner = new HashMap<>();
        this.onChange = this::onChange;
    }

    public ObservableMap(Map<K, V> source, BiConsumer<String, Map<K, V>> callback) {
        this();
        inner.putAll(source);

        this.callback = callback;
    }

    protected void onChange(String operation) {
        if (handle || operation == null)
            return;

        if (observable != null) {
            if (observable instanceof ObservableMap) {
                ((ObservableMap) observable).handle = true;
            }

            switch (operation) {
                case "clear":
                    observable.clear();
                    return;

                case "put":
                case "remove":
                case "addAll":
                case "retainAll":
                case "removeAll":
                    MapDifference<K, V> mapDifference = Maps.difference(inner, observable);

                    // removing not used keys
                    mapDifference.entriesOnlyOnRight().forEach((k, v) -> observable.remove(k));
                    //adding new keys
                    observable.putAll(mapDifference.entriesOnlyOnLeft());
                    // set new changes to map
                    mapDifference.entriesDiffering().forEach((k, vValueDifference) -> observable.put(k, vValueDifference.leftValue()));

                    return;
            }

            if (observable instanceof ObservableMap) {
                ((ObservableMap) observable).handle = false;
                BiConsumer consumer = ((ObservableMap) observable).callback;
                if (consumer != null) {
                    consumer.accept(operation, this);
                }
            }
        }

        if (callback != null) {
            callback.accept(operation, this);
        }
    }

    public ObservableMap<K, V> sync(Map<K, V> map) {
        observable = map;
        return this;
    }

    @Override
    public V put(K key, V value) {
        V put = inner.put(key, value);
        onChange.accept("put");
        return put;
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public V get(Object key) {
        return inner.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return inner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return inner.containsValue(value);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new ObservableSet<>(inner.entrySet(), onChange);
    }
}
