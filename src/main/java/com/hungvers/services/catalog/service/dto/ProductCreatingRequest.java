package com.hungvers.services.catalog.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class ProductCreatingRequest {
    private String name;
    private String sku;
    private String barcode;
    private UUID categoryId;
    private Set<String> images;
    private long sellingPrice;
    private Long costPrice;
}
