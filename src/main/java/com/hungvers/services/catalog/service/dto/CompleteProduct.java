package com.hungvers.services.catalog.service.dto;

import com.hungvers.services.catalog.model.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class CompleteProduct {
    private UUID id;
    private long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private ProductStatus status;

    private String name;
    private String sku;
    private String barcode;
    private UUID categoryId;
    private Set<String> images;
    private long sellingPrice;
    private Long costPrice;
}
