package com.experiment.consumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "service-provider")
public interface ProviderFeignClient {

    @GetMapping("/api/message")
    Map<String, String> getProviderMessage();

    @GetMapping("/api/hello")
    String hello();
}
