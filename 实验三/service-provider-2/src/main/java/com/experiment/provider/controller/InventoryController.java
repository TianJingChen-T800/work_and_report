package com.experiment.provider.controller;

import com.experiment.provider.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final List<Product> products = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(10);
    private final AtomicInteger normalCount = new AtomicInteger(0);

    public InventoryController() {
        products.add(new Product(10L, "固态硬盘1TB", 699.0));
        products.add(new Product(11L, "内存条16GB", 329.0));
        products.add(new Product(12L, "显示器27寸", 1899.0));
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
     * 可控失败端点：每3次请求中有2次返回500
     */
    @GetMapping("/unreliable")
    public ResponseEntity<?> unreliable() {
        int count = normalCount.incrementAndGet();
        if (count % 3 != 0) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Simulated inventory service failure"));
        }
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Inventory request succeeded"));
    }

    /**
     * 慢响应端点：固定延迟3秒
     */
    @GetMapping("/slow")
    public ResponseEntity<?> slow() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Slow inventory response after 3 seconds"));
    }
}
