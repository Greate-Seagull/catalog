package com.hungvers.services.catalog.integration.product;

import com.hungvers.services.catalog.common.cursor.Cursor;
import com.hungvers.services.catalog.common.exception.ResourceNotFoundException;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;
import com.hungvers.services.catalog.service.ProductService;
import com.hungvers.services.catalog.service.dto.CompleteProduct;
import com.hungvers.services.catalog.service.dto.ProductCreatingRequest;
import com.hungvers.services.catalog.service.dto.ProductLookUpQuery;
import com.hungvers.services.catalog.service.dto.ProductQuery;
import com.hungvers.services.catalog.service.port.ProductRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductReadIT {
    private final ProductRepository repo;
    private final ProductService sut;
    private List<UUID> prods = new ArrayList<>();
    private List<UUID> category = new ArrayList<>();

    @Autowired
    public ProductReadIT(ProductRepository repo, ProductService sut) {
        this.repo = repo;
        this.sut = sut;
    }

    @BeforeAll
    public void setUp() {
        var tempProds = new ArrayList<Product>();

        var catalog1 = UUID.randomUUID();
        var catalog2 = UUID.randomUUID();
        category.add(catalog1);
        category.add(catalog2);

        tempProds.add(sut.create(new ProductCreatingRequest()
                .setName("Nước rửa chén Sunlight")
                .setBarcode("1234567890")
                .setSku("NUOC")
                .setCategoryId(catalog1)
        ));
        tempProds.add(sut.create(new ProductCreatingRequest()
                .setName("Nước mắm Chinsu")
                .setBarcode("123456789013")
                .setSku("NUOC-MAM-2")
                .setCategoryId(catalog2)
        ));
        tempProds.add(sut.create(new ProductCreatingRequest()
                .setName("Nước mắm Nam Ngư 500ml")
                .setBarcode("123456789014")
                .setSku("NUOC-MAM")
                .setCategoryId(catalog2)
        ));
        tempProds.add(sut.create(new ProductCreatingRequest()
                .setName("Nước ngọt Pepsi lon")
                .setBarcode("4321324")
                .setSku("PSI-LON")
                .setCategoryId(catalog1)
        ));
        tempProds.add(sut.create(new ProductCreatingRequest()
                .setName("Nước suối Aquafina 500ml")
                .setBarcode("432132433")
                .setSku("AQF-500")
                .setCategoryId(catalog1)
        ));
        tempProds.add(sut.create(new ProductCreatingRequest()
                .setName("Bình nước inox 1L")
                .setBarcode("6423562")
                .setSku("BNH-1L")
                .setCategoryId(catalog2)
        ));
        tempProds.add(sut.create(new ProductCreatingRequest()
                .setName("Chai nước mắm Nam Ngư")
                .setBarcode("123456789015")
                .setSku("MAM-NN")
                .setCategoryId(catalog2)
        ));

        tempProds.set(2, sut.hide(tempProds.get(2).getId()));
        tempProds.set(3, sut.hide(tempProds.get(3).getId()));

        prods = tempProds.stream().map(Product::getId)
                .toList();
    }

    @AfterAll
    public void cleanUp() {
        repo.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"nước", "Nước", "nuoc", "Nuoc"})
    public void products_are_searched_correctly_by_q(String q) {
        var result = sut.search(
                new ProductQuery().setQ(q),
                Cursor.ofLimit(20)
        );

        assertThat(result.data())
                .extracting(CompleteProduct::getId)
                .containsExactly(prods.get(0), prods.get(2), prods.get(1), prods.get(4), prods.get(3), prods.get(6), prods.get(5));
    }

    @Test
    public void products_are_searched_correctly_by_catalog() {
        var result = sut.search(
                new ProductQuery().setCategoryId(category.getFirst()),
                Cursor.ofLimit(20)
        );

        assertThat(result.data())
                .extracting(CompleteProduct::getId)
                .containsExactly(prods.get(4), prods.get(3), prods.get(0));
    }

    @Test
    public void products_are_searched_correctly_by_status() {
        var result = sut.search(
                new ProductQuery().setStatus(ProductStatus.HIDDEN),
                Cursor.ofLimit(20)
        );

        assertThat(result.data())
                .extracting(CompleteProduct::getId)
                .containsExactly(prods.get(3), prods.get(2));
    }

    @Test
    public void products_are_searched_correctly_by_barcode() {
        var result = sut.search(
                new ProductQuery().setQ("12345"),
                Cursor.ofLimit(20)
        );

        assertThat(result.data())
                .extracting(CompleteProduct::getId)
                .containsExactly(prods.get(6), prods.get(2), prods.get(1), prods.get(0), prods.get(5), prods.get(4), prods.get(3));
    }

    @Test
    public void products_are_searched_correctly_by_combination() {
        var result = sut.search(
                new ProductQuery().setQ("nuoc")
                        .setCategoryId(category.getFirst())
                        .setStatus(ProductStatus.ACTIVE),
                Cursor.ofLimit(20)
        );

        assertThat(result.data())
                .extracting(CompleteProduct::getId)
                .containsExactly(prods.get(0), prods.get(4));
    }

    @Test
    public void product_is_looked_up_correctly_by_sku() {
        var result = sut.lookUp(
                new ProductLookUpQuery("MAM-NN", null)
        );

        assertThat(result).isEqualTo(prods.get(6));
    }

    @Test
    public void product_is_looked_up_correctly_by_barcode() {
        var result = sut.lookUp(
                new ProductLookUpQuery(null, "123456789014")
        );

        assertThat(result).isEqualTo(prods.get(2));
    }

    @Test
    public void product_is_looked_up_correctly_by_combination() {
        var result = sut.lookUp(
                new ProductLookUpQuery("AQF-500", "432132433")
        );

        assertThat(result).isEqualTo(prods.get(4));
    }

    @Test
    public void product_is_not_found() {
        ThrowableAssert.ThrowingCallable throwable = () -> sut.lookUp(
                new ProductLookUpQuery("AQF-500", "123456789013")
        );

        assertThatThrownBy(throwable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void look_up_returns_error_when_no_arguments() {
        ThrowableAssert.ThrowingCallable throwable = () -> sut.lookUp(
                new ProductLookUpQuery(null, null)
        );

        assertThatThrownBy(throwable).isInstanceOf(RuntimeException.class);
    }
}
