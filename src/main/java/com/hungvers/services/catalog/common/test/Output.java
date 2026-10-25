package com.hungvers.services.catalog.common.test;

public record Output<T>(String op, T result, Throwable error) {
    public static <T> Output<T> success(String op, T result) {
        return new Output<>(op, result, null);
    }

    public static <T> Output<T> failure(String op, Throwable error) {
        return new Output<>(op, null, error);
    }

    public boolean hasFailed() {
        return result == null && error != null;
    }

    public boolean hasSucceeded() {
        return result != null && error == null;
    }
}
