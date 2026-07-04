package com.experiment.consumer.config;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientSpecification;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalancerRegistrationConfig {

    private static final String SERVICE_NAME = "product-service";

    @Bean
    public LoadBalancerClientSpecification customLoadBalancerSpec() {
        return new LoadBalancerClientSpecification(SERVICE_NAME,
                new Class<?>[]{CustomLoadBalancerConfig.class});
    }
}
