package com.gim.capability.genshin;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.*;

public class ObservableSet<K> implements Set<K> {

    private final Set<K> inner;
    private final Consumer<String> onChange;


    public ObservableSet(Set<K> inner, Consumer<String> onChange) {
        this.inner = inner;
        this.onChange = onChange;
    }

    private void onChange(String id) {
        this.onChange.accept(id);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return inner.contains(o);
    }

    @NotNull
    @Override
    public Iterator<K> iterator() {
        return new ObservableIterator<>(inner.iterator(), this::onChange);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return inner.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return inner.toArray(a);
    }

    @Override
    public boolean add(K k) {
        if (inner.add(k)) {
            onChange("add");
            return true;
        }

        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (inner.remove(o)) {
            onChange("remove");
            return true;
        }

        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return inner.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends K> c) {
        if (inner.addAll(c)) {
            onChange("addAll");
            return true;
        }

        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        if (inner.retainAll(c)) {
            onChange("retainAll");
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        if (inner.removeAll(c)) {
            onChange("removeAll");
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        inner.clear();
        onChange("clear");
    }
}
