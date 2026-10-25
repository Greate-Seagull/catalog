package com.hungvers.services.catalog.unit.product;

import com.hungvers.services.catalog.common.product.ProductArranger;
import com.hungvers.services.catalog.common.config.TimeConfig;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductHideTest {
    @Test
    public void product_is_hidden() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());

        product.hide();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
    }

    @Test
    public void product_is_hidden_if_already_hidden() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        product.hide();

        product.hide();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
    }

    @Test
    public void product_is_not_hidden_if_already_deleted() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        product.delete(TimeConfig.CLOCK);

        ThrowableAssert.ThrowingCallable throwable = () -> product.hide();

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void product_is_unhidden() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        product.hide();

        product.unhide();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    public void product_is_unhidden_if_already_active() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());

        product.unhide();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    public void product_is_not_unhidden_if_already_deleted() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        product.delete(TimeConfig.CLOCK);

        ThrowableAssert.ThrowingCallable throwable = () -> product.unhide();

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }
}
