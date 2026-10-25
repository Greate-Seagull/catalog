package com.hungvers.services.catalog.common.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class CommonUtils {
    private CommonUtils() {
        /* This utility class should not be instantiated */
    }

    public static String normalize(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
        return n.isBlank() ? null : n;
    }

    public static final Pattern SKU_PATTERN =
            Pattern.compile("[a-z0-9_-]{1,64}", Pattern.CASE_INSENSITIVE);
    public static boolean isSku(String sku) {
        return sku != null && SKU_PATTERN.matcher(sku).matches();
    }

    public static final Pattern BARCODE_PATTERN =
            Pattern.compile("[a-z0-9]{1,32}", Pattern.CASE_INSENSITIVE);
    public static boolean isBarcode(String input) {
        return input != null && BARCODE_PATTERN.matcher(input).matches();
    }
}
