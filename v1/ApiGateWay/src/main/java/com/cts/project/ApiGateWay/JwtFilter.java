package com.cts.project.ApiGateWay;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/users/register",
            "/users/login",
            "/users/me",
            "/users/internal/",
            "/logs/internal/",
            "/reservations/internal/",
            "/slots/internal/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(req.getMethod())) {
            log.debug("OPTIONS preflight — skipping JWT | path={}", path);
            return chain.filter(exchange);
        }

        if (isPublicPath(path)) {
            log.debug("Public path — skipping JWT | path={}", path);
            return chain.filter(exchange);
        }

        String authHeader = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization header | path={}", path);
            return reject(exchange, "Authorization header is missing.");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid JWT | path={}", path);
            return reject(exchange, "Invalid or expired JWT token.");
        }

        String email = jwtUtil.extractEmail(token);
        String role  = jwtUtil.extractRole(token);
        log.debug("JWT valid | email={} | role={} | path={}", email, role, path);

        ServerHttpRequest mutatedReq = req.mutate()
                .header("X-Auth-Email", email)
                .header("X-Auth-Role", role)
                .header("X-Role", role)
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.filter(exchange.mutate().request(mutatedReq).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        ServerHttpResponse res = exchange.getResponse();
        res.setStatusCode(HttpStatus.UNAUTHORIZED);
        res.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String body = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), message
        );

        var buffer = res.bufferFactory().wrap(body.getBytes());
        return res.writeWith(Mono.just(buffer));
    }
}