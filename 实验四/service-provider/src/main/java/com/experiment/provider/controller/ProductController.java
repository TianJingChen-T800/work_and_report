package com.experiment.provider.controller;

import com.experiment.provider.model.Product;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final List<Product> products = Arrays.asList(
        new Product(1L, "笔记本电脑", 6999.00),
        new Product(2L, "机械键盘", 499.00),
        new Product(3L, "蓝牙耳机", 299.00),
        new Product(4L, "显示器", 1599.00),
        new Product(5L, "鼠标", 199.00)
    );

    @GetMapping
    public List<Product> list(HttpServletRequest request) {
        System.out.println("[Provider-1:8081] 处理请求: " + request.getRequestURI());
        return products;
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id, HttpServletRequest request) {
        System.out.println("[Provider-1:8081] 查询产品 ID=" + id);
        return products.stream().filter(p -> p.getId().equals(id))
                .findFirst().orElse(null);
    }

    @GetMapping("/port")
    public String port() {
        return "服务实例: service-provider, 端口: 8081";
    }
}
