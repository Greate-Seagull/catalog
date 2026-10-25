package com.hungvers.services.catalog.common.rest;

import java.util.Objects;
import java.util.function.Consumer;

public record Patch<T>(T value) {
    public static <T> void ifPresent(Patch<T> patch, Consumer<T> consumer) {
        if (patch != null)
            consumer.accept(patch.value());
    }

    public static <T> boolean setIfChanged(Patch<T> patch, T origin, Consumer<T> setter) {
        if (patch == null || Objects.equals(patch.value(), origin))
            return false;

        setter.accept(patch.value());
        return true;
    }

    public static <T> Patch<T> of(T value) {
        return new Patch<>(value);
    }
}
