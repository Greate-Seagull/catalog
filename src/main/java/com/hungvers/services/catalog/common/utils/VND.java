package com.hungvers.services.catalog.common.utils;

public record VND(long value) {
    public VND {
        if (value < 0 || value > 999_999_999_999L)
            throw new RuntimeException();
    }
}
