package com.experiment.gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/product")
    public Mono<Map<String, String>> productFallback() {
        return Mono.just(Map.of(
            "code", "503",
            "message", "产品服务暂不可用，已触发熔断保护，请稍后重试",
            "fallback", "true"
        ));
    }

    @RequestMapping("/fallback/inventory")
    public Mono<Map<String, String>> inventoryFallback() {
        return Mono.just(Map.of(
            "code", "503",
            "message", "库存服务暂不可用，已触发熔断保护，请稍后重试",
            "fallback", "true"
        ));
    }
}
