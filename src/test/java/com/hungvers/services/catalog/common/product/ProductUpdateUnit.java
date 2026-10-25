package com.hungvers.services.catalog.common.product;

import com.hungvers.services.catalog.common.config.TimeConfig;
import com.hungvers.services.catalog.model.Product;
import com.hungvers.services.catalog.service.dto.ProductUpdatingRequest;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Getter
public class ProductUpdateUnit {
    private String name = "Create Product";
    private String sku = "1234567890";
    private String barcode = "ABC123";
    private UUID categoryId = UUID.randomUUID();
    private Set<String> images = Set.of("image1");
    private Long sellingPrice = 100000L;
    private Long costPrice = 75000L;

    private ProductUpdatingRequest request = new ProductUpdatingRequest();
    private Product product;

    private ProductUpdateUnit() {

    }

    public static ProductUpdateUnit of(Product product) {
        var unit = new ProductUpdateUnit();
        unit.product = product;
        unit.name = product.getName();
        unit.sku = product.getSku();
        unit.barcode = product.getBarcode();
        unit.categoryId = product.getCategoryId();
        unit.images = product.getImages();
        unit.sellingPrice = product.getSellingPrice();
        unit.costPrice = product.getCostPrice();
        return unit;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
        request.setBarcode(barcode);
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
        request.setCategoryId(categoryId);
    }

    public void setCostPrice(Long costPrice) {
        this.costPrice = costPrice;
        request.setCostPrice(costPrice);
    }

    public void setImages(Set<String> images) {
        this.images = images;
        request.setImages(images);
    }

    public void setName(String name) {
        this.name = name;
        request.setName(name);
    }

    public void setSellingPrice(Long sellingPrice) {
        this.sellingPrice = sellingPrice;
        request.setSellingPrice(sellingPrice);
    }

    public void setSku(String sku) {
        this.sku = sku;
        request.setSku(sku);
    }

    public void act() {
        product.update(request);
    }

    public void assertOnlyModifiedFieldsChange() {
        act();
        assertThat(product.getName()).isEqualTo(this.getName());
        assertThat(product.getSku()).isEqualTo(this.getSku());
        assertThat(product.getBarcode()).isEqualTo(this.getBarcode());
        assertThat(product.getCategoryId()).isEqualTo(this.getCategoryId());
        assertThat(product.getImages()).isEqualTo(this.getImages());
        assertThat(product.getSellingPrice()).isEqualTo(this.getSellingPrice());
        assertThat(product.getCostPrice()).isEqualTo(this.getCostPrice());
    }
}
