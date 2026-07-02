package com.etiya.gatewayserver.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Gateway'i reactive (WebFlux) OAuth2 Resource Server yapar.
 *
 * - Actuator disinda gelen tum istekler gecerli bir Keycloak JWT'si gerektirir;
 *   token gecersiz/eksikse gateway 401 doner ve istek downstream'e hic ulasmaz
 *   (merkezi kimlik dogrulama katmani).
 * - Gecerli istegin Authorization header'i Spring Cloud Gateway tarafindan
 *   downstream servislere aynen iletilir; her servis token'i tekrar dogrular ve
 *   kendi rol bazli yetkilendirmesini uygular.
 * - Ince taneli rol kontrolu (GET/POST...) is servislerinde yapilir; gateway
 *   yalnizca "kimlik dogrulanmis mi" seviyesinde koruma saglar.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(reactiveJwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Keycloak realm rollerini ("realm_access.roles") ROLE_ onekli yetkilere cevirip
     * reactive akisa uyarlar.
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> reactiveJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter defaultScopes = new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = defaultScopes.convert(jwt);
            return realmRoles(jwt, authorities);
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> realmRoles(Jwt jwt, Collection<GrantedAuthority> base) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection)) {
            return base;
        }
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        List<GrantedAuthority> authorities = new java.util.ArrayList<>(base);
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        return authorities;
    }
}
