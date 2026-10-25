package com.hungvers.services.catalog.model;

import com.hungvers.services.catalog.common.rest.Patch;
import com.hungvers.services.catalog.common.utils.CommonUtils;
import com.hungvers.services.catalog.service.dto.ProductCreatingRequest;
import com.hungvers.services.catalog.service.dto.ProductUpdatingRequest;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products")

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Product {
    @Id
    private UUID id;
    // Read-only by client
    private long version = 0;
    private OffsetDateTime deletedAt;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private String name;
    private String sku;
    private String barcode;
    private UUID categoryId;
    private Set<String> images;
    private long sellingPrice; // Chưa bao gồm thuế
    private Long costPrice;
    // Chưa có chỉ số thuế
    public void setName(String name) {
        if (name == null || name.isBlank() || name.length() > 255)
            throw new RuntimeException();
        this.name = name.strip();
    }

    public void setSku(String sku) {
        if (sku == null) {
            this.sku = null;
            return;
        }
        var normalizedSku = sku.strip();
        if (!CommonUtils.isSku(normalizedSku))
            throw new RuntimeException();
        this.sku = normalizedSku;
    }

    public void setBarcode(String barcode) {
        if (barcode == null) {
            this.barcode = null;
            return;
        }
        var normalized = barcode.strip();
        if (!CommonUtils.isBarcode(normalized))
            throw new RuntimeException();
        this.barcode = normalized;
    }

    public void setSellingPrice(long sellingPrice) {
        if (sellingPrice < 0 || sellingPrice > 999_999_999_999L)
            throw new RuntimeException();
        this.sellingPrice = sellingPrice;
    }

    public void setCostPrice(Long costPrice) {
        if (costPrice == null)
            costPrice = 0L;
        if (costPrice < 0 || costPrice > 999_999_999_999L)
            throw new RuntimeException();
        this.costPrice = costPrice;
    }

    // Dùng được trong cả 2 trường hợp create và update
    public void setImages(Set<String> images) {
        if (images == null) {
            this.images = new HashSet<>();
            return;
        }
        if (images.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Images cannot contain null values");
        }
        this.images = new HashSet<>(images);
    }

    public static Product create(ProductCreatingRequest request) {
        Product product = new Product();
        // server generated
        product.setId(UUID.randomUUID());
        product.setStatus(ProductStatus.ACTIVE);
        // client provided
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setCategoryId(request.getCategoryId());
        product.setImages(request.getImages());
        product.setSellingPrice(request.getSellingPrice());
        product.setCostPrice(request.getCostPrice());
        return product;
    }

    public boolean update(ProductUpdatingRequest request) {
        boolean changed = false;
        changed |= Patch.setIfChanged(request.getName(), this.name, this::setName);
        changed |= Patch.setIfChanged(request.getSku(), this.sku, this::setSku);
        changed |= Patch.setIfChanged(request.getBarcode(), this.barcode, this::setBarcode);
        changed |= Patch.setIfChanged(request.getCategoryId(), this.categoryId, this::setCategoryId);
        changed |= Patch.setIfChanged(request.getImages(), this.images, this::setImages);
        changed |= Patch.setIfChanged(request.getSellingPrice(), this.sellingPrice, this::setSellingPrice);
        changed |= Patch.setIfChanged(request.getCostPrice(), this.costPrice, this::setCostPrice);
        return changed;
    }

    public void hide() {
        if (getStatus() == ProductStatus.DELETED)
            throw new RuntimeException();
        setStatus(ProductStatus.HIDDEN);
    }

    public void unhide() {
        if (getStatus() == ProductStatus.DELETED)
            throw new RuntimeException();
        setStatus(ProductStatus.ACTIVE);
    }

    public void delete(Clock clock) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        setStatus(ProductStatus.DELETED);
        setDeletedAt(now);
    }

    public void restore() {
        if (getStatus() != ProductStatus.DELETED)
            throw new RuntimeException();
        setStatus(ProductStatus.HIDDEN);
    }
}
