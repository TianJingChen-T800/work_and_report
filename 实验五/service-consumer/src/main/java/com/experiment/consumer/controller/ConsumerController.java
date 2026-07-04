package com.experiment.consumer.controller;

import com.experiment.consumer.feign.ProviderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RefreshScope
public class ConsumerController {

    @Autowired
    private ProviderFeignClient providerFeignClient;

    @Value("${app.message:Hello from Consumer}")
    private String message;

    @Value("${app.version:1.0}")
    private String version;

    @GetMapping("/api/consumer/message")
    public Map<String, String> getConsumerMessage() {
        return Map.of(
            "service", "service-consumer",
            "message", message,
            "version", version
        );
    }

    @GetMapping("/api/consumer/call-provider")
    public Map<String, Object> callProvider() {
        Map<String, String> providerMsg = providerFeignClient.getProviderMessage();
        return Map.of(
            "consumerMessage", getConsumerMessage(),
            "providerResponse", providerMsg
        );
    }

    @GetMapping("/api/consumer/hello")
    public String hello() {
        String providerHello = providerFeignClient.hello();
        return "Consumer says: message=" + message + ", version=" + version + " | Provider says: " + providerHello;
    }
}
