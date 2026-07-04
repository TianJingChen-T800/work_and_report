package com.experiment.provider.controller;

import com.experiment.provider.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final List<Product> products = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(1);
    private final AtomicInteger normalCount = new AtomicInteger(0);

    public ProductController() {
        products.add(new Product(1L, "笔记本电脑", 5999.0));
        products.add(new Product(2L, "机械键盘", 399.0));
        products.add(new Product(3L, "无线鼠标", 199.0));
    }

    @GetMapping
    public List<Product> getAll() {
        return products;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        product.setId((long) counter.getAndIncrement());
        products.add(product);
        return product;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (products.removeIf(p -> p.getId().equals(id))) {
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 可控失败端点：每3次请求中有2次返回500，用于测试失败率熔断
     */
    @GetMapping("/unreliable")
    public ResponseEntity<?> unreliable() {
        int count = normalCount.incrementAndGet();
        if (count % 3 != 0) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Simulated server error (failure injection)"));
        }
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Lucky request succeeded"));
    }

    /**
     * 慢响应端点：固定延迟3秒，用于测试慢调用熔断
     */
    @GetMapping("/slow")
    public ResponseEntity<?> slow() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Slow response after 3 seconds"));
    }
}
