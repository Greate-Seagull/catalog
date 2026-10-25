package com.hungvers.services.catalog.common.rest;

import java.util.function.Consumer;

@Deprecated
public final class Presented<T> {
    private T value;
    private boolean presented;

    public Presented(T value, boolean presented) {
        this.value = value;
        this.presented = presented;
    }

    public static <T> Presented<T> of(T value) {
        return new Presented<>(value, true);
    }

    public static <T> Presented<T> absent() {
        return new Presented<>(null, false);
    }

    public void present(T value) {
        this.value = value;
        this.presented = true;
    }

    public void ifPresent(Consumer<T> consumer) {
        if (presented)
            consumer.accept(value);
    }

    public T value() {
        return value;
    }

    public boolean presented() {
        return presented;
    }
}
