package com.hungvers.services.catalog.integration.product;

import com.hungvers.services.catalog.common.cursor.Cursor;
import com.hungvers.services.catalog.common.exception.DeadlockException;
import com.hungvers.services.catalog.common.exception.ResourceNotFoundException;
import com.hungvers.services.catalog.common.exception.UniquenessViolatedException;
import com.hungvers.services.catalog.common.product.ProductArranger;
import com.hungvers.services.catalog.common.test.ConcurrencyTest;
import com.hungvers.services.catalog.common.test.FixtureCleaner;
import com.hungvers.services.catalog.common.test.Input;
import com.hungvers.services.catalog.common.test.Output;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.model.ProductStatus;
import com.hungvers.services.catalog.service.ProductService;
import com.hungvers.services.catalog.service.dto.CompleteProduct;
import com.hungvers.services.catalog.service.dto.ProductCreatingRequest;
import com.hungvers.services.catalog.service.dto.ProductQuery;
import com.hungvers.services.catalog.service.dto.ProductUpdatingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.set;

@SpringBootTest
@ActiveProfiles("test")
public class ProductConcurrencyIT {
    private final FixtureCleaner cleaner;
    private final ProductService sut;

    @Autowired
    public ProductConcurrencyIT(FixtureCleaner cleaner, ProductService sut) {
        this.cleaner = cleaner;
        this.sut = sut;
    }

    @BeforeEach
    public void cleanUp() {
        cleaner.cleanAll();
    }

    @RepeatedTest(20)
    public void creation_without_sku_is_correct_under_stress() throws InterruptedException {
        var errors = ConcurrencyTest.fireConcurrentTasks(20, () -> sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku(null)
                .setBarcode(null)
        ));

        assertThat(errors).isEmpty();
        var prods = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(prods.data())
                .extracting(CompleteProduct::getSku)
                .containsExactlyInAnyOrder("SP000001", "SP000002", "SP000003", "SP000004", "SP000005",
                        "SP000006", "SP000007", "SP000008", "SP000009", "SP000010",
                        "SP000011", "SP000012", "SP000013", "SP000014", "SP000015",
                        "SP000016", "SP000017", "SP000018", "SP000019", "SP000020"
                );
    }

    @RepeatedTest(20)
    public void creation_with_same_sku_is_correct_under_stress() throws InterruptedException {
        var errors = ConcurrencyTest.fireConcurrentTasks(20, () -> sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku("AQUA")
                .setBarcode(null)
        ));

        assertThat(errors)
                .hasSize(19)
                .hasOnlyElementsOfType(UniquenessViolatedException.class);
        var prods = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(prods.data())
                .extracting(CompleteProduct::getSku)
                .containsExactlyInAnyOrder("AQUA");
    }

    @RepeatedTest(20)
    public void creation_with_same_barcode_is_correct_under_stress() throws InterruptedException {
        var errors = ConcurrencyTest.fireConcurrentTasks(20, () -> sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku(null)
                .setBarcode("123")
        ));

        assertThat(errors)
                .hasSize(19)
                .hasOnlyElementsOfType(UniquenessViolatedException.class);
        var prods = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(prods.data())
                .extracting(CompleteProduct::getBarcode)
                .containsExactlyInAnyOrder("123");
    }

    @RepeatedTest(20)
    public void updating_a_resource_with_same_version_is_correct_under_stress() throws InterruptedException {
        var created = sut.create(ProductArranger.createDefaultCreatingRequest());
        var rand = ThreadLocalRandom.current();
        var errors = ConcurrencyTest.fireConcurrentTasks(20, () -> sut.update(
                created.getId(),
                created.getVersion(),
                new ProductUpdatingRequest()
                        .setBarcode(String.valueOf(rand.nextLong(1000000000)))
                )
        );

        assertThat(errors)
                .hasSize(19)
                .hasOnlyElementsOfType(ResourceNotFoundException.class);
        var prods = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(prods.data())
                .hasSize(1);
    }

    @RepeatedTest(20)
    public void restoring_a_resource_is_correct_under_stress() throws InterruptedException {
        var created = sut.create(ProductArranger.createDefaultCreatingRequest());
        var deleted = sut.delete(created.getId());
        var errors = ConcurrencyTest.fireConcurrentTasks(20, () -> sut.restore(created.getId()));

        assertThat(errors).isEmpty();
        var prods = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(prods.data())
                .singleElement()
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(deleted.getId());
                    assertThat(u.getVersion()).isEqualTo(deleted.getVersion() + 1);
                });
    }

    @RepeatedTest(20)
    public void restoring_many_resources_with_same_sku_is_correct_under_stress() throws InterruptedException {
        int resourceCount = 20;
        String setSku = "API";
        Deque<UUID> deletedIds = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < resourceCount; i++) {
            var created = sut.create(ProductArranger.createDefaultCreatingRequest()
                    .setSku(setSku)
                    .setBarcode(null)
            );
            var deleted = sut.delete(created.getId());
            deletedIds.add(deleted.getId());
        }

        var errors = ConcurrencyTest.fireConcurrentTasks(resourceCount, () -> sut.restore(deletedIds.pop()));

        assertThat(errors).hasOnlyElementsOfType(UniquenessViolatedException.class);
        var prods = sut.search(
                new ProductQuery().setStatus(ProductStatus.HIDDEN),
                Cursor.ofLimit(20)
        );
        assertThat(prods.data())
                .extracting(CompleteProduct::getSku)
                .doesNotHaveDuplicates()
                .isSubsetOf(
                        "API", "API-dup-01", "API-dup-02", "API-dup-03", "API-dup-04", "API-dup-05",
                        "API-dup-06", "API-dup-07", "API-dup-08", "API-dup-09", "API-dup-10",
                        "API-dup-11", "API-dup-12", "API-dup-13", "API-dup-14", "API-dup-15",
                        "API-dup-16", "API-dup-17", "API-dup-18", "API-dup-19", "API-dup-20"
                );
    }

    @RepeatedTest(20)
    public void hiding_a_resource_is_correct_under_stress() throws InterruptedException {
        var created = sut.create(ProductArranger.createDefaultCreatingRequest());
        var errors = ConcurrencyTest.fireConcurrentTasks(20, () -> sut.hide(created.getId()));

        assertThat(errors).isEmpty();
        var prods = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(prods.data())
                .singleElement()
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(created.getId());
                    assertThat(u.getVersion()).isEqualTo(created.getVersion() + 1);
                });
    }

    @RepeatedTest(20)
    public void unhiding_a_resource_is_correct_under_stress() throws InterruptedException {
        var created = sut.create(ProductArranger.createDefaultCreatingRequest());
        var hidden = sut.hide(created.getId());
        var errors = ConcurrencyTest.fireConcurrentTasks(20, () -> sut.unhide(created.getId()));

        assertThat(errors).isEmpty();
        var prods = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(prods.data())
                .singleElement()
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(created.getId());
                    assertThat(u.getVersion()).isEqualTo(hidden.getVersion() + 1);
                });
    }

    @RepeatedTest(20)
    public void deleting_a_resource_is_correct_under_stress() throws InterruptedException {
        var created = sut.create(ProductArranger.createDefaultCreatingRequest());
        var errors = ConcurrencyTest.fireConcurrentTasks(20, () -> sut.delete(created.getId()));

        assertThat(errors).isEmpty();
        var prods = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(prods.data())
                .singleElement()
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(created.getId());
                    assertThat(u.getVersion()).isEqualTo(created.getVersion() + 1);
                });
    }

    @RepeatedTest(20)
    public void updating_hiding_unhiding_deleting_restoring_a_resource_is_correct_under_stress() throws InterruptedException {
        var created = sut.create(ProductArranger.createDefaultCreatingRequest());

        Deque<Input<Product>> tasks = new ConcurrentLinkedDeque<>();
        tasks.add(Input.of("update", () -> sut.update(created.getId(), created.getVersion(), ProductArranger.createDefaultUpdatingRequest())));
        tasks.add(Input.of("hide", () -> sut.hide(created.getId())));
        tasks.add(Input.of("unhide", () -> sut.unhide(created.getId())));
        tasks.add(Input.of("delete", () -> sut.delete(created.getId())));
        tasks.add(Input.of("restore", () -> sut.restore(created.getId())));
        var outputs = ConcurrencyTest.fireConcurrentDistinctTasks(tasks);

        var errors = outputs.stream().filter(Output::hasFailed)
                        .toList();
        var successes = outputs.stream().filter(Output::hasSucceeded)
                        .toList();
        var maxVer = successes.stream()
                .map(o -> o.result().getVersion())
                .max(Long::compareTo)
                .orElse(created.getVersion());

        assertThat(errors)
                .extracting(Output::error)
                .hasOnlyElementsOfType(ResourceNotFoundException.class);

        var saved = sut.get(created.getId());
        assertThat(saved.getStatus()).isIn(Arrays.stream(ProductStatus.values()).toList());
        assertThat(saved.getVersion()).isEqualTo(maxVer);
    }

    @RepeatedTest(20)
    public void inserting_updating_restoring_with_same_sku_is_correct_under_stress() throws InterruptedException {
        // Arrange
        var setSku = "API";

        var forUpdate = sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku("BeforeUpdate")
                .setBarcode(null)
        );
        var updatingRequest = new ProductUpdatingRequest().setSku(setSku);

        var forRestore = sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku(setSku)
                .setBarcode(null)
        );
        sut.delete(forRestore.getId());

        // Act
        Deque<Input<Product>> tasks = new ConcurrentLinkedDeque<>();
        tasks.add(Input.of("insert", () -> sut.create(
                ProductArranger.createDefaultCreatingRequest()
                                .setSku(setSku)
                                .setBarcode(null)
                )
        ));
        tasks.add(Input.of("update", () -> sut.update(forUpdate.getId(), forUpdate.getVersion(), updatingRequest)));
        tasks.add(Input.of("restore", () -> sut.restore(forRestore.getId())));
        var outputs = ConcurrencyTest.fireConcurrentDistinctTasks(tasks);

        // Assert
        var errors = outputs.stream().filter(Output::hasFailed)
                .toList();
        var successes = outputs.stream().filter(Output::hasSucceeded)
                .toList();

        assertThat(errors)
                .extracting(Output::error)
                .hasOnlyElementsOfType(UniquenessViolatedException.class);

        var result = sut.search(new ProductQuery().setStatus(ProductStatus.ACTIVE), Cursor.ofLimit(20));
        assertThat(result.data())
                .extracting(CompleteProduct::getSku)
                .doesNotHaveDuplicates();
    }

    @RepeatedTest(20)
    public void inserting_updating_restoring_with_same_barcode_is_correct_under_stress() throws InterruptedException {
        // Arrange
        var setBarcode = "0123";

        var forUpdate = sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku("1")
                .setBarcode("BeforeUpdate")
        );
        var updatingRequest = new ProductUpdatingRequest().setBarcode(setBarcode);

        var forRestore = sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku("2")
                .setBarcode(setBarcode)
        );
        sut.delete(forRestore.getId());

        // Act
        Deque<Input<Product>> tasks = new ConcurrentLinkedDeque<>();
        tasks.add(Input.of("insert", () -> sut.create(
                        ProductArranger.createDefaultCreatingRequest()
                                .setSku("3")
                                .setBarcode(setBarcode)
                )
        ));
        tasks.add(Input.of("update", () -> sut.update(forUpdate.getId(), forUpdate.getVersion(), updatingRequest)));
        tasks.add(Input.of("restore", () -> sut.restore(forRestore.getId())));
        var outputs = ConcurrencyTest.fireConcurrentDistinctTasks(tasks);

        // Assert
        var errors = outputs.stream().filter(Output::hasFailed)
                .toList();
        var successes = outputs.stream().filter(Output::hasSucceeded)
                .toList();

        assertThat(errors)
                .extracting(Output::error)
                .hasOnlyElementsOfType(UniquenessViolatedException.class);

        var result = sut.search(new ProductQuery().setStatus(ProductStatus.ACTIVE), Cursor.ofLimit(20));
        assertThat(result.data())
                .extracting(CompleteProduct::getBarcode)
                .doesNotHaveDuplicates();
    }

    @RepeatedTest(20)
    public void exchange_sku_in_update_is_correct_under_stress() throws InterruptedException {
        // Arrange
        var forUpdate1 = sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku("1")
                .setBarcode(null)
        );
        var forUpdate2 = sut.create(ProductArranger.createDefaultCreatingRequest()
                .setSku("2")
                .setBarcode(null)
        );

        // Act
        Deque<Input<Product>> tasks = new ConcurrentLinkedDeque<>();
        tasks.add(Input.of("update1", () -> sut.update(forUpdate1.getId(), forUpdate1.getVersion(), new ProductUpdatingRequest().setSku(forUpdate2.getSku()))));
        tasks.add(Input.of("update2", () -> sut.update(forUpdate2.getId(), forUpdate2.getVersion(), new ProductUpdatingRequest().setSku(forUpdate1.getSku()))));
        var outputs = ConcurrencyTest.fireConcurrentDistinctTasks(tasks);

        // Assert
        var errors = outputs.stream().filter(Output::hasFailed)
                .toList();

        assertThat(errors)
                .extracting(Output::error)
                .hasOnlyElementsOfTypes(UniquenessViolatedException.class, DeadlockException.class);

        var result = sut.search(new ProductQuery(), Cursor.ofLimit(20));
        assertThat(result.data())
                .extracting(CompleteProduct::getSku)
                .doesNotHaveDuplicates();
    }
}
