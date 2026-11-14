package com.schoolmanagement.gateway;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;

@Configuration(proxyBeanMethods = false)
public class GatewaySecurityConfiguration {
    @Bean
    ReactiveJwtDecoder gatewayJwtDecoder(@Value("${school.jwt.secret}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("SCHOOL_JWT_SECRET must be at least 32 bytes");
        }
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    ReactiveJwtAuthenticationConverter gatewayAuthorities() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) return Flux.empty();
            return Flux.fromIterable(roles)
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }

    @Bean
    SecurityWebFilterChain gatewaySecurity(ServerHttpSecurity http,
                                           ReactiveJwtAuthenticationConverter authorities) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers("/actuator/health", "/api/auth/login").permitAll()
                .pathMatchers(HttpMethod.PATCH, "/api/teachers/leaves/*/decision")
                    .hasAnyRole("SUPER_ADMIN", "PRINCIPAL")
                .pathMatchers(HttpMethod.POST, "/api/teachers")
                    .hasAnyRole("SUPER_ADMIN", "PRINCIPAL")
                .pathMatchers(HttpMethod.POST, "/api/timetables/**", "/api/substitutions/**")
                    .hasAnyRole("SUPER_ADMIN", "PRINCIPAL")
                .pathMatchers(HttpMethod.POST, "/api/inventory/**")
                    .hasAnyRole("SUPER_ADMIN", "STORE_MANAGER")
                .pathMatchers(HttpMethod.PATCH, "/api/inventory/**")
                    .hasAnyRole("SUPER_ADMIN", "STORE_MANAGER")
                .pathMatchers("/api/**").authenticated()
                .anyExchange().denyAll())
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(authorities)))
            .build();
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        cors.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return new CorsWebFilter(source);
    }
}
