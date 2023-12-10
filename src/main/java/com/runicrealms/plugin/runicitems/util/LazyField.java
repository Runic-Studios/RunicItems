package com.runicrealms.plugin.runicitems.util;

import java.util.function.Supplier;

// Because we don't use kotlin and can't have nice things D:
public final class LazyField<T> {

    private final Supplier<T> supplier;
    private volatile T value = null;

    public LazyField(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (value == null) {
            synchronized (this) { // Lock this
                if (value == null) {
                    value = supplier.get();
                }
            }
        }
        return value;
    }

}
