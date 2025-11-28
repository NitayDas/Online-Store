package com.example.onlinestore.controller;

import com.example.onlinestore.service.ProductService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) { this.productService = productService; }

    @GetMapping
    public ResponseEntity<?> all() {
        List<Map<String,Object>> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Map<String,Object> p = productService.getProductById(id);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> byCategory(@PathVariable String category) {
        List<Map<String,Object>> list = productService.getProductsByCategory(category);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> create(@RequestBody Map<String,Object> product) {
        Map<String,Object> created = productService.createProduct(product);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String,Object> product) {
        Map<String,Object> updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Map<String,Object> deleted = productService.deleteProduct(id);
        return ResponseEntity.ok(deleted);
    }
}
