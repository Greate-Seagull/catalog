package com.hungvers.services.catalog.service.dto;

import com.hungvers.services.catalog.model.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class ProductQuery {
    private String q;
    private UUID categoryId;
    private ProductStatus status;
}
