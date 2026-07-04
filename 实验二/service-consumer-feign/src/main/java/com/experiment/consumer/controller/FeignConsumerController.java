package com.experiment.consumer.controller;

import com.experiment.consumer.feign.ProductFeignClient;
import com.experiment.consumer.model.Product;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/consumer/feign")
public class FeignConsumerController {

    private final ProductFeignClient feignClient;

    public FeignConsumerController(ProductFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @GetMapping
    public List<Product> getAll() {
        return feignClient.getAll();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return feignClient.getById(id);
    }

    @PostMapping
    public String create(@RequestBody Product product) {
        return feignClient.create(product);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody Product product) {
        return feignClient.update(id, product);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return feignClient.delete(id);
    }
}
