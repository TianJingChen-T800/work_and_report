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
    Product getById(@PathVariable("id") Long id);

    @PostMapping("/api/products")
    String create(@RequestBody Product product);

    @PutMapping("/api/products/{id}")
    String update(@PathVariable("id") Long id, @RequestBody Product product);

    @DeleteMapping("/api/products/{id}")
    String delete(@PathVariable("id") Long id);
}
