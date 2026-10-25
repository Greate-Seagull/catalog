package com.hungvers.services.catalog.service.dto;

import com.hungvers.services.catalog.common.rest.Patch;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
public class ProductUpdatingRequest {
    private Patch<String> name;
    private Patch<String> sku;
    private Patch<String> barcode;
    private Patch<UUID> categoryId;
    private Patch<Set<String>> images;
    private Patch<Long> sellingPrice;
    private Patch<Long> costPrice;

    public ProductUpdatingRequest setName(String name) {
        this.name = Patch.of(name);
        return this;
    }

    public ProductUpdatingRequest setSku(String sku) {
        this.sku = Patch.of(sku);
        return this;
    }

    public ProductUpdatingRequest setBarcode(String barcode) {
        this.barcode = Patch.of(barcode);
        return this;
    }

    public ProductUpdatingRequest setCategoryId(UUID categoryId) {
        this.categoryId = Patch.of(categoryId);
        return this;
    }

    public ProductUpdatingRequest setImages(Set<String> images) {
        this.images = Patch.of(images);
        return this;
    }

    public ProductUpdatingRequest setSellingPrice(Long sellingPrice) {
        this.sellingPrice = Patch.of(sellingPrice);
        return this;
    }

    public ProductUpdatingRequest setCostPrice(Long costPrice) {
        this.costPrice = Patch.of(costPrice);
        return this;
    }
}
