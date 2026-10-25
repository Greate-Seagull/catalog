package com.hungvers.services.catalog.common.product;

import com.hungvers.services.catalog.service.dto.ProductCreatingRequest;
import com.hungvers.services.catalog.service.dto.ProductUpdatingRequest;

import java.util.Set;
import java.util.UUID;

public class ProductArranger {
    public static ProductCreatingRequest createDefaultCreatingRequest() {
        var request = new ProductCreatingRequest();
        request.setName("Create Product");
        request.setBarcode("1234567890");
        request.setSku("ABC123");
        request.setCategoryId(UUID.randomUUID());
        request.setImages(Set.of("image1"));
        request.setSellingPrice(100000);
        request.setCostPrice(75000L);
        return request;
    }

    public static ProductUpdatingRequest createDefaultUpdatingRequest() {
        var request = new ProductUpdatingRequest();
        request.setName("Update Product");
        request.setBarcode("0987654321");
        request.setSku("123CBA");
        request.setCategoryId(UUID.randomUUID());
        request.setImages(Set.of("id1"));
        request.setSellingPrice(70000L);
        request.setCostPrice(80000L);
        return request;
    }
}
