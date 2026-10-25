package com.hungvers.services.catalog.common.cursor;

public record Ranked<T> (T item, int rank) {
}
