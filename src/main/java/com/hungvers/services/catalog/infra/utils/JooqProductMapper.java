package com.hungvers.services.catalog.infra.utils;

import com.hungvers.services.catalog.infra.jooq.tables.records.ProductsRecord;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;
import com.hungvers.services.catalog.service.dto.CompleteProduct;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class JooqProductMapper {
    private JooqProductMapper() {
        /* This utility class should not be instantiated */
    }

    public static Product toEntity(ProductsRecord into) {
        if(into == null)
            return null;

        var product = new Product();
        product.setId(into.getId());
        // Load version to read
        product.setVersion(into.getVersion());
        product.setDeletedAt(into.getDeletedAt());

        product.setStatus(ProductStatus.valueOf(into.getStatus()));
        product.setName(into.getName());
        product.setSku(into.getSku());
        product.setBarcode(into.getBarcode());
        product.setCategoryId(into.getCategoryId());
        product.setImages(Arrays.stream(into.getImages()).collect(Collectors.toSet()));
        product.setSellingPrice(into.getSellingPrice());
        product.setCostPrice(into.getCostPrice());
        return product;
    }

    public static ProductsRecord toRecord(Product product) {
        if (product == null) {
            return null;
        }

        var rec = new ProductsRecord();
        rec.setId(product.getId());
        // Do not set version here
        rec.setDeletedAt(product.getDeletedAt());

        if (product.getStatus() != null) {
            rec.setStatus(product.getStatus().name());
        }

        rec.setName(product.getName());
        rec.setSku(product.getSku());
        rec.setBarcode(product.getBarcode());
        rec.setCategoryId(product.getCategoryId());

        if (product.getImages() != null) {
            rec.setImages(product.getImages().toArray(new String[0]));
        }

        rec.setSellingPrice(product.getSellingPrice());
        rec.setCostPrice(product.getCostPrice());

        return rec;
    }

    public static CompleteProduct toView(ProductsRecord into) {
        if(into == null)
            return null;

        var product = new CompleteProduct();
        product.setId(into.getId());
        product.setVersion(into.getVersion());
        product.setCreatedAt(into.getCreatedAt());
        product.setUpdatedAt(into.getUpdatedAt());
        product.setDeletedAt(into.getDeletedAt());

        product.setStatus(ProductStatus.valueOf(into.getStatus()));
        product.setName(into.getName());
        product.setSku(into.getSku());
        product.setBarcode(into.getBarcode());
        product.setCategoryId(into.getCategoryId());
        product.setImages(Set.of(into.getImages()));
        product.setSellingPrice(into.getSellingPrice());
        product.setCostPrice(into.getCostPrice());
        return product;
    }
}
