package com.hungvers.services.catalog.unit.product;

import com.hungvers.services.catalog.common.product.ProductArranger;
import com.hungvers.services.catalog.common.product.ProductUpdateUnit;
import com.hungvers.services.catalog.common.config.TimeConfig;
import com.hungvers.services.catalog.common.rest.Patch;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.service.dto.ProductUpdatingRequest;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductUpdateTest {
    @Test
    public void product_is_updated() {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();

        boolean changed = product.update(request);

        assertThat(changed).isTrue();
        Patch.ifPresent(request.getName(), name -> assertThat(product.getName()).isEqualTo(name));
        Patch.ifPresent(request.getSku(), sku -> assertThat(product.getSku()).isEqualTo(sku));
        Patch.ifPresent(request.getBarcode(), barcode -> assertThat(product.getBarcode()).isEqualTo(barcode));
        Patch.ifPresent(request.getCategoryId(), categoryId -> assertThat(product.getCategoryId()).isEqualTo(categoryId));
        Patch.ifPresent(request.getImages(), images -> assertThat(product.getImages()).isEqualTo(images));
        Patch.ifPresent(request.getSellingPrice(), sellingPrice -> assertThat(product.getSellingPrice()).isEqualTo(sellingPrice));
        Patch.ifPresent(request.getCostPrice(), costPrice -> assertThat(product.getCostPrice()).isEqualTo(costPrice));
    }

    public static Stream<Function<ProductUpdatingRequest, ProductUpdatingRequest>> provideUnchangedRequest() {
        Function<ProductUpdatingRequest, ProductUpdatingRequest> sameValues = request -> request;
        Function<ProductUpdatingRequest, ProductUpdatingRequest> noValues = request -> new ProductUpdatingRequest();
        Function<ProductUpdatingRequest, ProductUpdatingRequest> combined = request -> {
            var latter = new ProductUpdatingRequest();
            latter.setName(request.getName().value());
            latter.setCostPrice(request.getCostPrice().value());
            return latter;
        };
        return Stream.of(sameValues, noValues, combined);
    }

    @ParameterizedTest
    @MethodSource("provideUnchangedRequest")
    public void product_is_not_changed(Function<ProductUpdatingRequest, ProductUpdatingRequest> arranger) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        product.update(request);

        boolean changed = product.update(arranger.apply(request));

        assertThat(changed).isFalse();
    }

    public static Stream<Consumer<ProductUpdatingRequest>> provideDeletedRequired() {
        Consumer<ProductUpdatingRequest> deletedName = request -> request.setName(null);
        // Delegate checking missing sku to database
        return Stream.of(deletedName);
    }

    @ParameterizedTest
    @MethodSource("provideDeletedRequired")
    public void product_is_not_updated_if_required_field_is_deleted(Consumer<ProductUpdatingRequest> arranger) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        arranger.accept(request);

        ThrowableAssert.ThrowingCallable throwable = () -> product.update(request);

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    public static Stream<Consumer<ProductUpdateUnit>> provideModifiedFields() {
        Consumer<ProductUpdateUnit> name = test -> test.setName("New name");
        Consumer<ProductUpdateUnit> sku = test -> test.setSku("SP0002");
        Consumer<ProductUpdateUnit> barcode = test -> test.setBarcode("01235323");
        Consumer<ProductUpdateUnit> categoryId = test -> test.setCategoryId(UUID.randomUUID());
        Consumer<ProductUpdateUnit> images = test -> test.setImages(Set.of("con cho"));
        Consumer<ProductUpdateUnit> sellingPrice = test -> test.setSellingPrice(10L);
        Consumer<ProductUpdateUnit> costPrice = test -> test.setCostPrice(20L);
        return Stream.of(name, sku, barcode, categoryId, images, sellingPrice, costPrice);
    }

    @ParameterizedTest
    @MethodSource("provideModifiedFields")
    public void product_is_updated_only_name(Consumer<ProductUpdateUnit> arranger) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var test = ProductUpdateUnit.of(product);
        arranger.accept(test);

        test.assertOnlyModifiedFieldsChange();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ab"
    })
    public void product_is_not_updated_with_invalid_name(String malformedName) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        request.setName(malformedName);

        ThrowableAssert.ThrowingCallable throwable = () -> product.update(request);

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    public static Stream<Arguments> provideNameAndExpected() {
        return Stream.of(
                Arguments.of(" new product ", "new product"),
                Arguments.of("hỗ trợ tiếng việt", "hỗ trợ tiếng việt"),
                Arguments.of(" hỗ trợ tiếng việt ", "hỗ trợ tiếng việt")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNameAndExpected")
    public void product_is_updated_with_auto_normalized_name(String provided, String expected) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        request.setName(provided);

        product.update(request);

        assertThat(product.getName()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "!@#$%^&*()_+-=[]{}|;':\",./<>?~!@#$%^&*()_+-=[]{}|;':\",./<>?~!@#$",
            "3g9K2sL8pQ4mX7vN1zC6wB0jH5fD9tR3yF2gH7jK4lZ1xV6bN9mC2xZ5qW8eR3tY1uI"
    })
    public void product_is_not_updated_with_invalid_sku(String provided) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        request.setSku(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> product.update(request);

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "!@#$%^&*()_+-=[]{}|;':\",./<>?~!@#$%^&*()_+-=[]{}|;':\",./<>?~!@#$",
            "3g9K2sL8pQ4mX7vN1zC6wB0jH5fD9tR3yF2gH7jK4lZ1xV6bN9mC2xZ5qW8eR3tY1uI"
    })
    public void product_is_not_updated_with_invalid_barcode(String provided) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        request.setBarcode(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> product.update(request);

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    public static Stream<Set<String>> provideInvalidImages() {
        HashSet<String> nullContained = new HashSet<>();
        nullContained.add(null);
        return Stream.of(
                nullContained
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidImages")
    public void product_is_not_updated_with_invalid_images(Set<String> provided) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        request.setImages(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> product.update(request);

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    public static Stream<Arguments> provideStrangeImages() {
        HashSet<String> duplicatedInput = new HashSet<>();
        duplicatedInput.add("id1");
        duplicatedInput.add("id1");
        return Stream.of(
                Arguments.of(null, Set.of()),
                Arguments.of(Set.of(), Set.of()),
                Arguments.of(duplicatedInput, Set.of("id1"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideStrangeImages")
    public void product_is_updated_with_corrected_images(Set<String> provided, Set<String> expected) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        request.setImages(provided);

        product.update(request);

        assertThat(product.getImages()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 1_000_000_000_000L})
    public void product_is_not_updated_with_invalid_selling_price(long provided) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        request.setSellingPrice(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> product.update(request);

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 1_000_000_000_000L})
    public void product_is_not_updated_with_invalid_cost_price(long provided) {
        var product = Product.create(ProductArranger.createDefaultCreatingRequest());
        var request = ProductArranger.createDefaultUpdatingRequest();
        request.setCostPrice(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> product.update(request);

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }
}
