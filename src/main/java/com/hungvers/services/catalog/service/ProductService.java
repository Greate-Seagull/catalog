package com.hungvers.services.catalog.service;

import com.hungvers.services.catalog.common.cursor.Cursor;
import com.hungvers.services.catalog.common.cursor.CursorCodec;
import com.hungvers.services.catalog.common.cursor.CursorPage;
import com.hungvers.services.catalog.common.cursor.Ranked;
import com.hungvers.services.catalog.common.exception.ResourceNotFoundException;
import com.hungvers.services.catalog.service.dto.CompleteProduct;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;
import com.hungvers.services.catalog.service.dto.*;
import com.hungvers.services.catalog.service.port.ProductProjection;
import com.hungvers.services.catalog.service.port.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ProductService {
    private final Clock clock;
    private final ProductRepository repo;
    private final ProductProjection proj;
    private final CursorCodec codec;

    public ProductService(Clock clock, ProductRepository repo, ProductProjection proj, CursorCodec codec) {
        this.clock = clock;
        this.repo = repo;
        this.proj = proj;
        this.codec = codec;
    }

    @Transactional
    public Product create(ProductCreatingRequest request) {
        Product product = Product.create(request);
        return repo.insert(product);
    }

    @Transactional
    public Product update(UUID id, long version, ProductUpdatingRequest request) {
        Product product = getUndeleted(id);
        boolean changed = product.update(request);
        if (changed)
            return repo.update(version, product);
        return product;
    }

    @Transactional
    public Product hide(UUID id) {
        return repo.hide(id);
    }

    @Transactional
    public Product unhide(UUID id) {
        return repo.unhide(id);
    }

    @Transactional
    public Product delete(UUID id) {
        return repo.delete(id);
    }

    @Transactional
    public Product restore(UUID id) {
        return repo.restore(id, clock);
    }

    public Product get(UUID id) {
        return repo.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public Product getUndeleted(UUID id) {
        return repo.findByIdAndStatusNot(id, ProductStatus.DELETED).orElseThrow(ResourceNotFoundException::new);
    }

    public CursorPage<CompleteProduct> search(ProductQuery query, Cursor cursor) {
        var providedCur = codec.decode(cursor.cursor(), ProductCursor.class);
        var rankedProds = proj.find(query, providedCur, cursor.limit());
        var lastProd = rankedProds.getLast();
        var nextCur = new ProductCursor(lastProd.rank(), lastProd.item().getCreatedAt(), lastProd.item().getId());
        var encodedCur = codec.encode(nextCur);
        return new CursorPage<>(
                rankedProds.stream()
                        .map(Ranked::item)
                        .toList(),
                encodedCur
        );
    }

    public UUID lookUp(ProductLookUpQuery query) {
        if (query.isEmpty())
            throw new RuntimeException();
        return proj.findById(query).orElseThrow(ResourceNotFoundException::new);
    }
}
