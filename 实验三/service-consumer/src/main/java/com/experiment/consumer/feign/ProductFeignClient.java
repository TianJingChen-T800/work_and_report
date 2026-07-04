package com.experiment.consumer.feign;

import com.experiment.consumer.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/api/products")
    List<Product> getAll();

    @GetMapping("/api/products/{id}")
    Product getById(@PathVariable Long id);

    @PostMapping("/api/products")
    Product create(@RequestBody Product product);

    @DeleteMapping("/api/products/{id}")
    void delete(@PathVariable Long id);

    @GetMapping("/api/products/unreliable")
    String getUnreliable();

    @GetMapping("/api/products/slow")
    String getSlow();
}
