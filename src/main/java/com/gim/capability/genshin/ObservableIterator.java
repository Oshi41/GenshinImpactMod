package com.gim.capability.genshin;

import java.util.Iterator;
import java.util.function.Consumer;

public class ObservableIterator<K> implements Iterator<K> {

    private final Iterator<K> inner;
    private final Consumer<String> onChange;

    public ObservableIterator(Iterator<K> inner, Consumer<String> onChange) {

        this.inner = inner;
        this.onChange = onChange;
    }

    @Override
    public boolean hasNext() {
        return inner.hasNext();
    }

    @Override
    public K next() {
        return inner.next();
    }

    @Override
    public void remove() {
        inner.remove();
        onChange.accept("remove");
    }

    @Override
    public void forEachRemaining(Consumer<? super K> action) {
        inner.forEachRemaining(action);
    }
}
