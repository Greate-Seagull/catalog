package com.hungvers.services.catalog.service.port;

import com.hungvers.services.catalog.common.exception.UniquenessViolatedException;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

//public interface ProductRepository extends JpaRepository<Product, UUID> {
//    Optional<Product> findByIdAndStatusNot(UUID id, ProductStatus productStatus);
//}

public interface ProductRepository {
    Optional<Product> findByIdAndStatusNot(UUID id, ProductStatus productStatus);

    Product insert(Product product) throws UniquenessViolatedException;

    Optional<Product> findById(UUID id);

    Product update(long version, Product product) throws UniquenessViolatedException;

    void deleteAll();

    Product restore(UUID id, Clock clock);

    Product update(Product product) throws UniquenessViolatedException;

    Product hide(UUID id);

    Product unhide(UUID id);

    Product delete(UUID id);
}