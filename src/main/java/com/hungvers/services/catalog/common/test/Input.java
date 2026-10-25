package com.hungvers.services.catalog.common.test;

import java.util.function.Supplier;

public record Input<T>(String op, Supplier<T> task) {
    public static <T> Input<T> of(String op, Supplier<T> task) {
        return new Input<>(op, task);
    }
}
