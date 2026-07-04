package com.experiment.consumer.controller;

import com.experiment.consumer.feign.InventoryFeignClient;
import com.experiment.consumer.feign.ProductFeignClient;
import com.experiment.consumer.model.Product;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Supplier;

@RestController
@RequestMapping("/consumer")
public class ResilienceController {

    private final ProductFeignClient productClient;
    private final InventoryFeignClient inventoryClient;
    private final CircuitBreakerRegistry cbRegistry;
    private final BulkheadRegistry bhRegistry;
    private final RateLimiterRegistry rlRegistry;

    private CircuitBreaker circuitBreakerA;
    private CircuitBreaker circuitBreakerB;
    private Bulkhead bulkhead;
    private RateLimiter rateLimiter;

    public ResilienceController(ProductFeignClient productClient,
                                InventoryFeignClient inventoryClient,
                                CircuitBreakerRegistry cbRegistry,
                                BulkheadRegistry bhRegistry,
                                RateLimiterRegistry rlRegistry) {
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.cbRegistry = cbRegistry;
        this.bhRegistry = bhRegistry;
        this.rlRegistry = rlRegistry;
    }

    @PostConstruct
    public void init() {
        this.circuitBreakerA = cbRegistry.circuitBreaker("circuitBreakerA");
        this.circuitBreakerB = cbRegistry.circuitBreaker("circuitBreakerB");
        this.bulkhead = bhRegistry.bulkhead("threadPoolBulkhead");
        this.rateLimiter = rlRegistry.rateLimiter("rateLimiterA");
    }

    // ==================== 断路器A（失败率30%） ====================

    @GetMapping("/products")
    public List<Product> getProducts() {
        return executeWithCB(circuitBreakerA,
                () -> productClient.getAll(),
                () -> List.of(new Product(-1L, "降级-产品列表不可用", 0.0)));
    }

    @GetMapping("/products/{id}")
    public Product getProductById(@PathVariable Long id) {
        return executeWithCB(circuitBreakerA,
                () -> productClient.getById(id),
                () -> new Product(id, "降级-产品详情不可用", 0.0));
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product p) {
        return executeWithCB(circuitBreakerA,
                () -> productClient.create(p),
                () -> new Product(-1L, "降级-创建失败", 0.0));
    }

    @DeleteMapping("/products/{id}")
    public Map<String, Object> deleteProduct(@PathVariable Long id) {
        return executeWithCB(circuitBreakerA,
                () -> { productClient.delete(id); return Map.of("status","OK","message","Deleted"); },
                () -> Map.of("status","FALLBACK","message","降级-删除失败"));
    }

    @GetMapping("/products/unreliable")
    public Map<String, Object> getUnreliableProducts() {
        return executeWithCB(circuitBreakerA,
                () -> {
                    String result = productClient.getUnreliable();
                    return Map.of("status", "OK", "data", result);
                },
                () -> Map.of("status","FALLBACK","message","断路器A已打开，触发服务降级"));
    }

    // ==================== 断路器B（失败率50%+慢调用30%@2s） ====================

    @GetMapping("/inventory")
    public List<Product> getInventory() {
        return executeWithCB(circuitBreakerB,
                () -> inventoryClient.getAll(),
                () -> List.of(new Product(-2L, "降级-库存列表不可用", 0.0)));
    }

    @GetMapping("/inventory/{id}")
    public Product getInventoryById(@PathVariable Long id) {
        return executeWithCB(circuitBreakerB,
                () -> inventoryClient.getById(id),
                () -> new Product(id, "降级-库存详情不可用", 0.0));
    }

    @GetMapping("/inventory/unreliable")
    public Map<String, Object> getUnreliableInventory() {
        return executeWithCB(circuitBreakerB,
                () -> {
                    String result = inventoryClient.getUnreliable();
                    return Map.of("status", "OK", "data", result);
                },
                () -> Map.of("status","FALLBACK","message","断路器B已打开，触发服务降级"));
    }

    @GetMapping("/inventory/slow")
    public Map<String, Object> getSlowInventory() {
        return executeWithCB(circuitBreakerB,
                () -> {
                    String result = inventoryClient.getSlow();
                    return Map.of("status", "OK", "data", result);
                },
                () -> Map.of("status","FALLBACK","message","断路器B（慢调用熔断）已打开，触发服务降级"));
    }

    // ==================== 隔离器（max concurrent=10, max wait=20ms） ====================

    @GetMapping("/products/isolated")
    public Map<String, Object> getIsolatedProducts() {
        Supplier<Map<String, Object>> supplier = Bulkhead.decorateSupplier(bulkhead, () -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return Map.of("status", "OK", "data", productClient.getAll());
        });
        try {
            return supplier.get();
        } catch (io.github.resilience4j.bulkhead.BulkheadFullException e) {
            return Map.of("status","FALLBACK","message","线程池隔离器已满，触发服务降级（并发超限）");
        } catch (Exception e) {
            return Map.of("status","FALLBACK","message","隔离器降级: "+e.getClass().getSimpleName());
        }
    }

    // ==================== 限流器（2s窗口，5个请求） ====================

    @GetMapping("/inventory/limited")
    public Map<String, Object> getLimitedInventory() {
        Supplier<Map<String, Object>> supplier = RateLimiter.decorateSupplier(rateLimiter, () ->
                Map.of("status", "OK", "data", inventoryClient.getAll()));
        try {
            return supplier.get();
        } catch (io.github.resilience4j.ratelimiter.RequestNotPermitted e) {
            return Map.of("status","FALLBACK","message","限流器触发，请求被拒绝");
        } catch (Exception e) {
            return Map.of("status","FALLBACK","message","限流器降级: "+e.getClass().getSimpleName());
        }
    }

    // ==================== 内部工具方法 ====================

    private <T> T executeWithCB(CircuitBreaker cb, Supplier<T> supplier, Supplier<T> fallback) {
        try {
            return cb.executeSupplier(supplier);
        } catch (Exception e) {
            return fallback.get();
        }
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return Map.of(
                "service", "consumer-resilience",
                "port", 8080,
                "cbA_state", circuitBreakerA.getState().name(),
                "cbB_state", circuitBreakerB.getState().name(),
                "endpoints", List.of(
                        "/consumer/products - 断路器A",
                        "/consumer/products/unreliable - 断路器A 失败测试",
                        "/consumer/products/isolated - 线程池隔离器",
                        "/consumer/inventory - 断路器B",
                        "/consumer/inventory/unreliable - 断路器B 失败测试",
                        "/consumer/inventory/slow - 断路器B 慢调用测试",
                        "/consumer/inventory/limited - 限流器",
                        "/consumer/status - 本页面"
                )
        );
    }
}
