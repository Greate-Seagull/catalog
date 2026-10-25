package com.hungvers.services.catalog.controller;

import com.hungvers.services.catalog.service.dto.CompleteProduct;
import com.hungvers.services.catalog.service.dto.ProductCreatingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/products")
public class ProductController {
    @PostMapping
    public ResponseEntity<CompleteProduct> create(@RequestBody ProductCreatingRequest product) {
        return null;
    }
}
