package com.schoolmanagement.gateway;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiRateLimitFilter implements GlobalFilter, Ordered {
    // This local cache limits one gateway process; deployment across replicas needs shared state.
    private final Cache<String, AtomicInteger> counts = Caffeine.newBuilder()
        .maximumSize(100_000).expireAfterWrite(Duration.ofMinutes(1)).build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith("/api/")) return chain.filter(exchange);
        String address = exchange.getRequest().getRemoteAddress() == null ? "unknown" :
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        int limit = path.equals("/api/auth/login") ? 10 : 120;
        String key = address + ':' + (path.equals("/api/auth/login") ? "login" : "api");
        int count = counts.get(key, ignored -> new AtomicInteger()).incrementAndGet();
        if (count <= limit) return chain.filter(exchange);
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"timestamp\":\"" + Instant.now() +
            "\",\"status\":429,\"errorCode\":\"RATE_LIMITED\",\"message\":\"Too many requests\",\"path\":\"" +
            path + "\"}";
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() { return -1; }
}
