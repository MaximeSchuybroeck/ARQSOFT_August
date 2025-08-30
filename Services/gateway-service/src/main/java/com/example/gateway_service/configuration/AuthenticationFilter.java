package com.example.gateway_service.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenValidator tokenValidator;

    @Value("${security.permitTestRoutes:false}")
    private boolean permitTestRoutes;

    @Autowired
    public AuthenticationFilter(JwtTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final ServerHttpRequest req = exchange.getRequest();
        final String path = req.getURI().getPath();
        final HttpMethod method = req.getMethod();

        // Always open auth endpoints
        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }

        // Whitelist for NFR tests (enable via security.permitTestRoutes=true)
        if (permitTestRoutes && isPublicTestRoute(path, method)) {
            return chain.filter(exchange);
        }

        // JWT required beyond this point
        String authHeader = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            tokenValidator.validateToken(token);
        } catch (Exception e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        return chain.filter(exchange);
    }

    private boolean isPublicTestRoute(String path, HttpMethod method) {
        if (path.startsWith("/actuator/")) return true;
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs")) return true;
        if (HttpMethod.OPTIONS.equals(method)) return true;

        // allow read-only GETs during the NFR test
        if (permitTestRoutes && HttpMethod.GET.equals(method)) {
            if (path.startsWith("/api/books")) return true;        // pretty path (when you add the explicit route)
            if (path.startsWith("/book-service/")) return true;    // discovery path (works today)
        }
        return false;
    }


    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() { return -1; }
}
