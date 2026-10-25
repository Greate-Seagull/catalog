package com.hungvers.services.catalog.common.cursor;

public record Cursor(String cursor, int limit) {
    public static Cursor ofLimit(int limit) {
        return new Cursor(null, limit);
    }
}
