package com.hungvers.services.catalog.infra.projection.jooq;

import com.hungvers.services.catalog.common.cursor.Ranked;
import com.hungvers.services.catalog.common.utils.CommonUtils;
import com.hungvers.services.catalog.infra.utils.JooqProductMapper;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.service.dto.CompleteProduct;
import com.hungvers.services.catalog.service.dto.ProductCursor;
import com.hungvers.services.catalog.service.dto.ProductLookUpQuery;
import com.hungvers.services.catalog.service.dto.ProductQuery;
import com.hungvers.services.catalog.service.port.ProductProjection;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.hungvers.services.catalog.infra.jooq.Tables.PRODUCTS;
import static org.jooq.impl.DSL.lower;

@Component
public class JooqProductProjection implements ProductProjection {
    private final DSLContext dsl;

    public JooqProductProjection(@Qualifier("replicaDsl") DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Ranked<CompleteProduct>> find(ProductQuery query, ProductCursor cursor, int limit) {
        Condition filters = DSL.and(
                JooqCondition.skipWhen(PRODUCTS.CATEGORY_ID::eq, query.getCategoryId()),
                JooqCondition.skipWhenEnum(PRODUCTS.STATUS::eq, query.getStatus())
        );

        String normalizedQ = CommonUtils.normalize(query.getQ());
        Field<String> loweredSku = lower(PRODUCTS.SKU);
        Field<String> loweredBarcode = lower(PRODUCTS.BARCODE);
        Field<Integer> rank = DSL.case_()
                .when(loweredSku.eq(normalizedQ).or(loweredBarcode.eq(normalizedQ)), 1)
                .when(loweredSku.startsWith(normalizedQ).or(loweredBarcode.startsWith(normalizedQ)), 2)
                .when(PRODUCTS.NAME_SEARCH.startsWith(normalizedQ), 3)
                .when(PRODUCTS.NAME_SEARCH.contains(normalizedQ), 4)
                .else_(5).as("rank");

        var ordered = dsl.select(PRODUCTS.asterisk(), rank)
                .from(PRODUCTS)
                .where(filters)
                .orderBy(rank.asc(), PRODUCTS.CREATED_AT.desc(), PRODUCTS.ID.asc());

        ResultQuery<Record> q = cursor == null
                ? ordered.limit(limit + 1)
                : ordered.seek(
                        cursor.rank(),
                        cursor.createdAt(),
                        cursor.id())
                .limit(limit + 1);

        return q.fetch(r -> new Ranked<>(
                JooqProductMapper.toView(r.into(PRODUCTS)),
                r.get(rank))
        );
    }

    @Override
    public Optional<UUID> findById(ProductLookUpQuery query) {
        Condition filters = DSL.and(
                JooqCondition.skipWhen(PRODUCTS.SKU::eq, query.sku()),
                JooqCondition.skipWhen(PRODUCTS.BARCODE::eq, query.barcode())
        );

        return dsl.select(PRODUCTS.ID)
                .from(PRODUCTS)
                .where(filters)
                .limit(1)
                .fetchOptional(PRODUCTS.ID);
    }
}
