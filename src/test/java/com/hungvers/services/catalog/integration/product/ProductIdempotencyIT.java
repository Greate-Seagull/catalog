package com.hungvers.services.catalog.integration.product;

import com.hungvers.services.catalog.common.cursor.Cursor;
import com.hungvers.services.catalog.common.exception.UniquenessViolatedException;
import com.hungvers.services.catalog.common.product.ProductArranger;
import com.hungvers.services.catalog.common.test.FixtureCleaner;
import com.hungvers.services.catalog.service.ProductService;
import com.hungvers.services.catalog.service.dto.CompleteProduct;
import com.hungvers.services.catalog.service.dto.ProductQuery;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductIdempotencyIT {
    private final FixtureCleaner cleaner;
    private final ProductService sut;

    @Autowired
    public ProductIdempotencyIT(FixtureCleaner cleaner, ProductService sut) {
        this.cleaner = cleaner;
        this.sut = sut;
    }

    @BeforeEach
    public void cleanUp() {
        cleaner.cleanAll();
    }

    // Test does not support without sku or barcode
    @Test
    public void product_created_is_saved_once_with_presence_of_sku_or_barcode() {
        var request = ProductArranger.createDefaultCreatingRequest();

        var product = sut.create(request);
        ThrowableAssert.ThrowingCallable throwable = () -> sut.create(request);

        assertThatThrownBy(throwable).isInstanceOf(UniquenessViolatedException.class);
        var saved = sut.search(new ProductQuery(), new Cursor(null, 20));
        assertThat(saved.data())
                .extracting(CompleteProduct::getId)
                .containsExactly(product.getId());
    }

    @Test
    public void product_updated_is_saved_only_once_when_version_matched() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);
        var updatingRequest = ProductArranger.createDefaultUpdatingRequest();

        var former = sut.update(created.getId(), created.getVersion(), updatingRequest);
        var latter = sut.update(created.getId(), former.getVersion(), updatingRequest);

        var saved = sut.get(former.getId());
        assertThat(saved).isEqualTo(former);
        assertThat(latter).isEqualTo(former);
    }

    @Test
    public void product_updated_is_saved_only_once_when_no_version_matched() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);
        var updatingRequest = ProductArranger.createDefaultUpdatingRequest();

        var former = sut.update(created.getId(), created.getVersion(), updatingRequest);
        var latter = sut.update(created.getId(), created.getVersion(), updatingRequest);

        var saved = sut.get(former.getId());
        assertThat(saved).isEqualTo(former);
        assertThat(latter).isEqualTo(former);
    }

    @Test
    public void product_hidden_is_saved_only_once() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);
        var former = sut.hide(created.getId());

        var latter = sut.hide(created.getId());

        var saved = sut.get(former.getId());
        assertThat(saved).isEqualTo(former);
        assertThat(latter).isEqualTo(former);
    }

    @Test
    public void product_unhidden_is_saved_only_once() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);
        sut.hide(created.getId());

        var former = sut.unhide(created.getId());
        var latter = sut.unhide(created.getId());

        var saved = sut.get(former.getId());
        assertThat(saved).isEqualTo(former);
        assertThat(latter).isEqualTo(former);
    }

    @Test
    public void product_is_deleted_only_once() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);

        var former = sut.delete(created.getId());
        var latter = sut.delete(created.getId());

        var saved = sut.get(former.getId());
        assertThat(saved).isEqualTo(former);
        assertThat(latter).isEqualTo(former);
    }

    @Test
    public void product_is_restored_only_once() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);
        sut.delete(created.getId());

        var former = sut.restore(created.getId());
        var latter = sut.restore(created.getId());

        var saved = sut.get(former.getId());
        assertThat(saved).isEqualTo(former);
        assertThat(latter).isEqualTo(former);
    }
}
