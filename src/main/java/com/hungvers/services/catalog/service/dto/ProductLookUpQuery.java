package com.hungvers.services.catalog.service.dto;

public record ProductLookUpQuery(String sku, String barcode) {
    public boolean isEmpty() {
        return sku == null && barcode == null;
    }
}
