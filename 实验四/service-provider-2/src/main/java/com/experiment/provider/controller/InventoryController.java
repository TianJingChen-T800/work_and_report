package com.experiment.provider.controller;

import com.experiment.provider.model.Product;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final List<Product> products = Arrays.asList(
        new Product(1L, "笔记本电脑", 50),
        new Product(2L, "机械键盘", 200),
        new Product(3L, "蓝牙耳机", 150),
        new Product(4L, "显示器", 80),
        new Product(5L, "鼠标", 300)
    );

    @GetMapping
    public List<Product> list(HttpServletRequest request) {
        System.out.println("[Provider-2:8082] 处理请求: " + request.getRequestURI());
        return products;
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id, HttpServletRequest request) {
        System.out.println("[Provider-2:8082] 查询库存 ID=" + id);
        return products.stream().filter(p -> p.getId().equals(id))
                .findFirst().orElse(null);
    }

    @GetMapping("/port")
    public String port() {
        return "服务实例: service-provider-2, 端口: 8082";
    }
}
