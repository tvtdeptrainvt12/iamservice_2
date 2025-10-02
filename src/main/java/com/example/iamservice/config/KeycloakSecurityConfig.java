package com.example.iamservice.config;

import com.example.iamservice.config.security.CustomJwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true")
public class KeycloakSecurityConfig {

    private final AppConfig appConfig;
    private final CustomJwtAuthConverter customJwtAuthConverter;

    @Bean
    public SecurityFilterChain keycloakFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthConverter)));
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true")
    public JwtDecoder jwtDecoder() {
        String issuerUri = appConfig.getKeycloakAuthUrl() + "/realms/" + appConfig.getRealm();
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}

