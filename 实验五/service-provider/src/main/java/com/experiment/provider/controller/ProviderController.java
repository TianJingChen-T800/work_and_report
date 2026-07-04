package com.experiment.provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RefreshScope
public class ProviderController {

    @Value("${app.message:Hello from Provider}")
    private String message;

    @Value("${app.version:1.0}")
    private String version;

    @GetMapping("/api/message")
    public Map<String, String> getMessage() {
        return Map.of(
            "service", "service-provider",
            "message", message,
            "version", version
        );
    }

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from Provider! message=" + message + ", version=" + version;
    }
}
