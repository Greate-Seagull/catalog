package com.hungvers.services.catalog.service.port;

import com.hungvers.services.catalog.common.cursor.Ranked;
import com.hungvers.services.catalog.service.dto.CompleteProduct;
import com.hungvers.services.catalog.service.dto.ProductCursor;
import com.hungvers.services.catalog.service.dto.ProductLookUpQuery;
import com.hungvers.services.catalog.service.dto.ProductQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductProjection {
    List<Ranked<CompleteProduct>> find(ProductQuery query, ProductCursor cursor, int limit);

    Optional<UUID> findById(ProductLookUpQuery query);
}
