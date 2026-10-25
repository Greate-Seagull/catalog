package com.hungvers.services.catalog.unit.product;

import com.hungvers.services.catalog.common.product.ProductArranger;
import com.hungvers.services.catalog.common.config.TimeConfig;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductDeleteTest {
    @Test
    public void product_is_deleted() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());

        product.delete(TimeConfig.CLOCK);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    public void product_is_deleted_if_already_deleted() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        product.delete(TimeConfig.CLOCK);

        product.delete(TimeConfig.CLOCK);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    public void product_is_restore_in_hidden() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        product.delete(TimeConfig.CLOCK);

        product.restore();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
    }

    @Test
    public void product_is_not_restored_if_not_deleted() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());

        ThrowableAssert.ThrowingCallable throwable = product::restore;

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }
}
