package com.experiment.consumer.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignErrorConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder() {
            private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

            @Override
            public Exception decode(String methodKey, Response response) {
                if (response.status() >= 500) {
                    return new RuntimeException(
                            "Remote service error: HTTP " + response.status() + " for " + methodKey);
                }
                return defaultDecoder.decode(methodKey, response);
            }
        };
    }
}
