package com.hungvers.services.catalog.infra.persistence.jooq;

import com.hungvers.services.catalog.common.db.DatabaseCodeCatalog;
import com.hungvers.services.catalog.common.exception.ConnectionException;
import com.hungvers.services.catalog.common.exception.DeadlockException;
import com.hungvers.services.catalog.common.exception.ResourceNotFoundException;
import com.hungvers.services.catalog.common.exception.UniquenessViolatedException;
import com.hungvers.services.catalog.infra.utils.JooqProductMapper;
import com.hungvers.services.catalog.infra.jooq.tables.Products;
import com.hungvers.services.catalog.infra.jooq.tables.records.ProductsRecord;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;
import com.hungvers.services.catalog.service.port.ProductRepository;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.hungvers.services.catalog.infra.jooq.tables.Products.PRODUCTS;

@Repository
@Primary
public class JooqProductRepository implements ProductRepository {
    private final DSLContext dsl;

    public JooqProductRepository(@Qualifier("primaryDsl") DSLContext dsl) {
        this.dsl = dsl;
    }

    private <T> T withUniqueViolationMapping(Supplier<T> action) {
        try {
            return action.get();
        } catch (DataAccessException e) {
            if (DatabaseCodeCatalog.UNIQUE_VIOLATION_CODE.equals(e.sqlState()))
                throw new UniquenessViolatedException(e);
            if (DatabaseCodeCatalog.DEADLOCK_CODE.equals(e.sqlState()))
                throw new DeadlockException(e);
            throw new ConnectionException(e);
        }
    }

    private Product updateTemplate(
            Product product,
            Condition extraCondition
    ) {
        ProductsRecord prodRec = JooqProductMapper.toRecord(product);

        return withUniqueViolationMapping(() -> dsl.update(PRODUCTS)
                .set(prodRec)
                .where(PRODUCTS.ID.eq(prodRec.getId()))
                .and(extraCondition)
                .returning()
                .fetchOptional()
                .map(JooqProductMapper::toEntity)
                .orElseThrow(ResourceNotFoundException::new)
        );
    }

    @Override
    public Optional<Product> findByIdAndStatusNot(UUID id, ProductStatus productStatus) {
        return dsl.selectFrom(PRODUCTS)
                .where(PRODUCTS.ID.eq(id))
                .and(PRODUCTS.STATUS.ne(productStatus.name()))
                .fetchOptional(JooqProductMapper::toEntity);
    }

    @Override
    public Product insert(Product product) throws UniquenessViolatedException {
        ProductsRecord prodRec = JooqProductMapper.toRecord(product);
        return withUniqueViolationMapping(() -> {
            ProductsRecord saved = dsl.insertInto(PRODUCTS)
                    .set(prodRec)
                    .set(PRODUCTS.SKU, DSL.coalesce(
                            DSL.val(prodRec.getSku(), PRODUCTS.SKU),
                            DSL.function("generate_value", String.class,
                                    DSL.field("cast({0} as regclass)", Object.class, DSL.val(PRODUCTS.getName())),
                                    DSL.field("cast({0} as name)", Object.class, DSL.val(PRODUCTS.SKU.getName())),
                                    DSL.val("SP%s"),
                                    DSL.val(6)
                            )
                    ))
                    .returning()
                    .fetchSingle();

            return JooqProductMapper.toEntity(saved);
        });
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return dsl.selectFrom(PRODUCTS)
                .where(PRODUCTS.ID.eq(id))
                .fetchOptional(JooqProductMapper::toEntity);
    }

    @Override
    public Product update(long version, Product product) throws UniquenessViolatedException {
        return updateTemplate(product, PRODUCTS.VERSION.eq(version));
    }


    @Override
    public Product update(Product product) throws UniquenessViolatedException {
        return updateTemplate(product, DSL.noCondition());
    }

    @Override
    public Product hide(UUID id) {
        return withUniqueViolationMapping(
                () -> setStatusIfIdMatches(id, ProductStatus.HIDDEN)
        );
    }

    @Override
    public Product unhide(UUID id) {
        return withUniqueViolationMapping(
                () -> setStatusIfIdMatches(id, ProductStatus.ACTIVE)
        );
    }

    // No update when not found but start another trip to retrieve row
    // Two trips
    private Product setStatusIfStatusNotMatches(UUID id, ProductStatus status) {
        return dsl.update(PRODUCTS)
                .set(PRODUCTS.STATUS, status.name())
                .where(PRODUCTS.ID.eq(id))
                .and(PRODUCTS.STATUS.ne(status.name()))
                .and(PRODUCTS.STATUS.ne(ProductStatus.DELETED.name()))
                .returning()
                .fetchOptional()
                .map(JooqProductMapper::toEntity)
                .orElseGet(() -> findByIdAndStatusNot(id, ProductStatus.DELETED)
                        .orElseThrow(ResourceNotFoundException::new)
                );
    }

    // Always update even if status does not match, as long as id matches
    // One trip
    private Product setStatusIfIdMatches(UUID id, ProductStatus status) {
        return dsl.update(PRODUCTS)
                .set(PRODUCTS.STATUS,
                        DSL.when(PRODUCTS.STATUS.ne(status.name()), DSL.val(status.name()))
                                .otherwise(PRODUCTS.STATUS))
                .where(PRODUCTS.ID.eq(id))
                .and(PRODUCTS.STATUS.ne(ProductStatus.DELETED.name()))
                .returning()
                .fetchOptional()
                .map(JooqProductMapper::toEntity)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Product delete(UUID id) {
        return withUniqueViolationMapping(
                () -> setStatusIfIdMatches(id, ProductStatus.DELETED)
        );
    }

    @Override
    public void deleteAll() {
        dsl.truncate(PRODUCTS)
                .execute();
    }

    // This operation does not guarantee success when there are concurrent updates violating uniqueness constraints.
    // Instead, it may throw a unique constraint violation exception.
    // Client should handle the exception and retry the operation if necessary.
    @Override
    public Product restore(UUID providedId, Clock clock) {
        ProductStatus selectedStatus = ProductStatus.DELETED;
        String selectedStatusName = selectedStatus.name();

        Products sub = PRODUCTS.as("sub");

        Field<String> baseSku = DSL.replace(
                DSL.regexpReplaceFirst(PRODUCTS.SKU, "-dup-\\d+$", ""),
                "%", "%%");

        var generateSku = DSL.function("generate_value", String.class,
                DSL.field("cast({0} as regclass)", Object.class, DSL.val(PRODUCTS.getName())),
                DSL.field("cast({0} as name)", Object.class, DSL.val(PRODUCTS.SKU.getName())),
                DSL.concat(baseSku, DSL.inline("-dup-%s")),
                DSL.val(2),
                DSL.val("products.sku.dup"),
                DSL.val(String.format("status <> '%s'", selectedStatusName))
        );

        Condition skuTaken = DSL.exists(
                DSL.selectOne().from(sub)
                        .where(DSL.lower(sub.SKU).eq(DSL.lower(PRODUCTS.SKU)))
                        .and(sub.STATUS.ne(selectedStatusName)));

        Condition barcodeTaken = PRODUCTS.BARCODE.isNotNull().and(DSL.exists(
                DSL.selectOne().from(sub)
                        .where(DSL.lower(sub.BARCODE).eq(DSL.lower(PRODUCTS.BARCODE)))
                        .and(sub.STATUS.ne(selectedStatusName))));

        return withUniqueViolationMapping(
                () -> dsl.update(PRODUCTS)
                .set(PRODUCTS.STATUS, ProductStatus.HIDDEN.name())
                .set(PRODUCTS.SKU, DSL.when(skuTaken, generateSku).else_(PRODUCTS.SKU))
                .set(PRODUCTS.BARCODE, DSL.when(barcodeTaken, DSL.inline((String) null)).else_(PRODUCTS.BARCODE))
                .where(PRODUCTS.ID.eq(providedId))
                .and(PRODUCTS.STATUS.eq(selectedStatusName))
                .returning()
                .fetchOptional()
                .map(JooqProductMapper::toEntity)
                .orElseGet(() -> findByIdAndStatusNot(providedId, selectedStatus)
                        .orElseThrow(ResourceNotFoundException::new)
                )
        );
    }
}
