package com.hungvers.services.catalog.unit.product;

import com.hungvers.services.catalog.common.product.ProductArranger;
import com.hungvers.services.catalog.common.config.TimeConfig;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;
import com.hungvers.services.catalog.service.dto.ProductCreatingRequest;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductCreationTest {

    @Test
    public void product_is_created() {
        var request = ProductArranger.createDefaultCreatingRequest();

        var product = Product.create(request);

        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo(request.getName());
        assertThat(product.getSku()).isEqualTo(request.getSku());
        assertThat(product.getBarcode()).isEqualTo(request.getBarcode());
        assertThat(product.getCategoryId()).isEqualTo(request.getCategoryId());
        assertThat(product.getImages()).isEqualTo(request.getImages());
        assertThat(product.getSellingPrice()).isEqualTo(request.getSellingPrice());
        assertThat(product.getCostPrice()).isEqualTo(request.getCostPrice());
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    public void product_is_created_with_minimum_required_fields() {
        var request = ProductArranger.createDefaultCreatingRequest()
                .setBarcode(null)
                .setCategoryId(null)
                .setCostPrice(null)
                .setImages(null)
                ;

        var product = Product.create(request);

        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo(request.getName());
        assertThat(product.getSku()).isEqualTo(request.getSku());
        assertThat(product.getBarcode()).isNull();
        assertThat(product.getCategoryId()).isNull();
        assertThat(product.getImages()).isEmpty();
        assertThat(product.getSellingPrice()).isEqualTo(request.getSellingPrice());
        assertThat(product.getCostPrice()).isZero();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    public static Stream<Consumer<ProductCreatingRequest>> provideMissingRequired() {
        Consumer<ProductCreatingRequest> missingName = request -> request.setName(null);
        return Stream.of(missingName);
    }

    @ParameterizedTest
    @MethodSource("provideMissingRequired")
    public void product_missing_required_is_not_created(Consumer<ProductCreatingRequest> arranger) {
        var request = ProductArranger.createDefaultCreatingRequest();
        arranger.accept(request);

        ThrowableAssert.ThrowingCallable throwable = () -> Product.create(request);

        assertThatThrownBy(throwable)
            .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ab"
    })
    public void product_with_invalid_name_is_not_created(String malformedName) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setName(malformedName);

        ThrowableAssert.ThrowingCallable throwable = () -> Product.create(request);

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
    public void product_is_created_with_supported_name(String provided, String expected) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setName(provided);

        var product = Product.create(request);

        assertThat(product.getName()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "  ",
            "!@#$%^&*()_+-=[]{}|;':\",./<>?~!@#$%^&*()_+-=[]{}|;':\",./<>?~!@#$",
            "3g9K2sL8pQ4mX7vN1zC6wB0jH5fD9tR3yF2gH7jK4lZ1xV6bN9mC2xZ5qW8eR3tY1uI"
    })
    public void product_is_not_created_with_invalid_sku(String provided) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setSku(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> Product.create(request);

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
    public void product_is_not_created_with_invalid_barcode(String provided) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setBarcode(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> Product.create(request);

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
    public void product_is_not_created_with_invalid_images(Set<String> provided) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setImages(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> Product.create(request);

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
    public void product_is_created_with_corrected_images(Set<String> provided, Set<String> expected) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setImages(provided);

        var product = Product.create(request);

        assertThat(product.getImages()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 1_000_000_000_000L})
    public void product_is_not_created_with_invalid_selling_price(long provided) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setSellingPrice(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> Product.create(request);

        assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 999_999_999_999L})
    public void product_is_created_with_supported_selling_price(long provided) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setSellingPrice(provided);

        var product = Product.create(request);

        assertThat(product.getSellingPrice()).isEqualTo(provided);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 1_000_000_000_000L})
    public void product_is_not_created_with_invalid_cost_price(long provided) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setCostPrice(provided);

        ThrowableAssert.ThrowingCallable throwable = () -> Product.create(request);

            assertThatThrownBy(throwable)
                .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 999_999_999_999L})
    public void product_is_created_with_supported_cost_price(long provided) {
        var request = ProductArranger.createDefaultCreatingRequest();
        request.setCostPrice(provided);

        var product = Product.create(request);

        assertThat(product.getCostPrice()).isEqualTo(provided);
    }
}
