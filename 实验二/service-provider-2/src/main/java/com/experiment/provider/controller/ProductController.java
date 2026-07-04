package com.experiment.provider.controller;

import com.experiment.provider.model.Product;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final String instance = "Provider2-8082";

    public ProductController() {
        store.put(1L, new Product(1L, "笔记本电脑", 5999.0));
        store.put(2L, new Product(2L, "机械键盘", 399.0));
    }

    @GetMapping
    public Collection<Product> getAll() {
        return store.values();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        Product p = store.get(id);
        if (p == null) throw new RuntimeException("Product not found: " + id);
        return p;
    }

    @PostMapping
    public String create(@RequestBody Product product) {
        store.put(product.getId(), product);
        return "[" + instance + "] Created: " + product.getName();
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody Product product) {
        if (!store.containsKey(id)) throw new RuntimeException("Product not found: " + id);
        product.setId(id);
        store.put(id, product);
        return "[" + instance + "] Updated: " + product.getName();
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        Product removed = store.remove(id);
        if (removed == null) throw new RuntimeException("Product not found: " + id);
        return "[" + instance + "] Deleted: " + removed.getName();
    }
}
