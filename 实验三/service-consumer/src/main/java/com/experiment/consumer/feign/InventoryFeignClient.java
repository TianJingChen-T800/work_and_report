package com.experiment.consumer.feign;

import com.experiment.consumer.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface InventoryFeignClient {

    @GetMapping("/api/inventory")
    List<Product> getAll();

    @GetMapping("/api/inventory/{id}")
    Product getById(@PathVariable Long id);

    @PostMapping("/api/inventory")
    Product create(@RequestBody Product product);

    @DeleteMapping("/api/inventory/{id}")
    void delete(@PathVariable Long id);

    @GetMapping("/api/inventory/unreliable")
    String getUnreliable();

    @GetMapping("/api/inventory/slow")
    String getSlow();
}
