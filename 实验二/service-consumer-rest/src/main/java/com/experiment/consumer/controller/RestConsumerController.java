package com.experiment.consumer.controller;

import com.experiment.consumer.model.Product;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@RestController
@RequestMapping("/consumer/rest")
public class RestConsumerController {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://product-service/api/products";

    public RestConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public List<Product> getAll() {
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                BASE_URL, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return restTemplate.getForObject(BASE_URL + "/" + id, Product.class);
    }

    @PostMapping
    public String create(@RequestBody Product product) {
        return restTemplate.postForObject(BASE_URL, product, String.class);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody Product product) {
        restTemplate.put(BASE_URL + "/" + id, product);
        return "Updated product " + id + " via RestTemplate";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        restTemplate.delete(BASE_URL + "/" + id);
        return "Deleted product " + id + " via RestTemplate";
    }
}
