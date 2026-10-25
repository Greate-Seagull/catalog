package com.hungvers.services.catalog.integration.product;

import com.hungvers.services.catalog.common.product.ProductArranger;
import com.hungvers.services.catalog.common.exception.ResourceNotFoundException;
import com.hungvers.services.catalog.common.exception.UniquenessViolatedException;
import com.hungvers.services.catalog.common.test.FixtureCleaner;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.service.ProductService;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductWriteIT {
    private final FixtureCleaner cleaner;
    private final ProductService sut;

    @Autowired
    public ProductWriteIT(FixtureCleaner cleaner, ProductService sut) {
        this.cleaner = cleaner;
        this.sut = sut;
    }

    @BeforeEach
    public void cleanUp() {
        cleaner.cleanAll();
    }

    @Test
    public void product_created_is_saved() {
        var request = ProductArranger.createDefaultCreatingRequest();

        var product = sut.create(request);

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_created_is_not_saved_when_sku_is_duplicate() {
        var request1 = ProductArranger.createDefaultCreatingRequest()
                .setBarcode("1");
        sut.create(request1);
        var request2 = ProductArranger.createDefaultCreatingRequest()
                .setBarcode("2");

        ThrowableAssert.ThrowingCallable throwable = () -> sut.create(request2);

        assertThatThrownBy(throwable).isInstanceOf(UniquenessViolatedException.class);
    }

    @Test
    public void product_created_is_not_saved_when_barcode_is_duplicate() {
        var request1 = ProductArranger.createDefaultCreatingRequest()
                .setSku("SP1");
        sut.create(request1);
        var request2 = ProductArranger.createDefaultCreatingRequest()
                .setSku("SP2");

        ThrowableAssert.ThrowingCallable throwable = () -> sut.create(request2);

        assertThatThrownBy(throwable).isInstanceOf(UniquenessViolatedException.class);
    }

    @Test
    public void product_created_is_saved_when_more_than_one_barcode_is_missing() {
        var request1 = ProductArranger.createDefaultCreatingRequest()
                .setBarcode(null)
                .setSku("SP1");
        sut.create(request1);
        var request2 = ProductArranger.createDefaultCreatingRequest()
                .setBarcode(null)
                .setSku("SP2");

        var product = sut.create(request2);

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    public Stream<Arguments> provideInsensitiveCases() {
        return Stream.of(
                Arguments.of("SP1", "SP1"),
                Arguments.of("SP1", "sp1"),
                Arguments.of("SP1", "sP1")
        );
    }
    
    @ParameterizedTest
    @MethodSource("provideInsensitiveCases")
    public void product_created_is_saved_when_sku_is_duplicate_but_deleted(String sku1, String sku2) {
        var request1 = ProductArranger.createDefaultCreatingRequest()
                .setSku(sku1)
                .setBarcode("1");
        var tempProd = sut.create(request1);
        sut.delete(tempProd.getId());
        var request2 = ProductArranger.createDefaultCreatingRequest()
                .setSku(sku2)
                .setBarcode("2");

        var product = sut.create(request2);

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    @ParameterizedTest
    @MethodSource("provideInsensitiveCases")
    public void product_created_is_saved_when_barcode_is_duplicate_but_deleted(String barcode1, String barcode2) {
        var request1 = ProductArranger.createDefaultCreatingRequest()
                .setSku("SP1")
                .setBarcode(barcode1);
        var tempProd = sut.create(request1);
        sut.delete(tempProd.getId());
        var request2 = ProductArranger.createDefaultCreatingRequest()
                .setSku("SP2")
                .setBarcode(barcode2);

        var product = sut.create(request2);

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_created_is_saved_with_generated_sku_when_sku_is_missing() {
        var request = ProductArranger.createDefaultCreatingRequest()
                .setSku(null);

        var product = sut.create(request);

        var saved = sut.get(product.getId());
        assertThat(saved.getSku()).isEqualTo("SP000001");
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_updated_is_saved() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);

        var updatingRequest = ProductArranger.createDefaultUpdatingRequest();
        var product = sut.update(created.getId(), created.getVersion(), updatingRequest);

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_deleted_is_not_modified() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);
        var id = created.getId();
        sut.delete(id);

        var updatingRequest = ProductArranger.createDefaultUpdatingRequest();
        ThrowableAssert.ThrowingCallable callable = () -> sut.update(id, created.getVersion(), updatingRequest);

        assertThatThrownBy(callable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void product_hidden_is_saved() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);

        var product = sut.hide(created.getId());

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_deleted_is_not_hidden() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var id = sut.create(creatingRequest).getId();
        sut.delete(id);

        ThrowableAssert.ThrowingCallable callable = () -> sut.hide(id);

        assertThatThrownBy(callable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void product_unhidden_is_saved() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);

        sut.hide(created.getId());
        var product = sut.unhide(created.getId());

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_deleted_is_not_unhidden() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var id = sut.create(creatingRequest).getId();
        sut.delete(id);

        ThrowableAssert.ThrowingCallable callable = () -> sut.unhide(id);

        assertThatThrownBy(callable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void product_deleted_is_saved() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);

        var product = sut.delete(created.getId());

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_restored_is_saved() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);

        sut.delete(created.getId());
        var product = sut.restore(created.getId());

        var saved = sut.get(product.getId());
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_restored_with_duplicated_sku_is_saved_with_suffixed_sku() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest()
                .setSku("PROD");
        var created = sut.create(creatingRequest);
        sut.delete(created.getId());
        sut.create(creatingRequest);

        var product = sut.restore(created.getId());

        var saved = sut.get(product.getId());
        assertThat(saved.getSku()).isEqualTo("PROD-dup-01");
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_restored_with_duplicated_barcode_is_saved_with_null_barcode() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);
        sut.delete(created.getId());
        sut.create(creatingRequest);

        var product = sut.restore(created.getId());

        var saved = sut.get(product.getId());
        assertThat(saved.getBarcode()).isNull();
        assertThat(saved).isEqualTo(product);
    }

    @Test
    public void product_deleted_is_not_shown_by_get_undeleted() {
        var creatingRequest = ProductArranger.createDefaultCreatingRequest();
        var created = sut.create(creatingRequest);

        sut.delete(created.getId());
        ThrowableAssert.ThrowingCallable callable = () -> sut.getUndeleted(created.getId());

        assertThatThrownBy(callable)
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
