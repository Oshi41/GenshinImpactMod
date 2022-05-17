package com.gim.other;

import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CustomLazy<T> implements Lazy<T> {
    private Supplier<T> supplier;
    private T instance;

    public CustomLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Nullable
    @Override
    public final T get() {
        if (supplier != null) {
            instance = supplier.get();
        }
        return instance;
    }

    public void reload() {
        instance = null;
    }
}
