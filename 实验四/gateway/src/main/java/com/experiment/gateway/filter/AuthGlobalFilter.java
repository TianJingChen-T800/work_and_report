package com.experiment.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        System.out.println("[Gateway 全局认证过滤器] 请求路径: " + path + ", Authorization: " + token);

        // 放行静态资源和健康检查端点
        if (path.startsWith("/actuator") || path.startsWith("/static")) {
            return chain.filter(exchange);
        }

        // 权限认证：必须携带 Authorization 请求头，值以 "Bearer " 开头
        if (token == null || token.isEmpty()) {
            System.out.println("[Gateway 全局认证过滤器] 拒绝访问: 缺少 Authorization 请求头");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if (!token.startsWith("Bearer ")) {
            System.out.println("[Gateway 全局认证过滤器] 拒绝访问: 认证格式错误，需要 Bearer token");
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        System.out.println("[Gateway 全局认证过滤器] 认证通过");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级，先于其他过滤器执行
    }
}
