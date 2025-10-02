package com.example.iamservice.config.security;

import com.example.iamservice.service.keycloakservice.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserSyncService userSyncService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Đồng bộ user vào DB (giống GoogleAuthService.handleGoogleLogin)
        userSyncService.syncUserFromToken(jwt);

        // Lấy roles từ claim realm_access.roles
        Collection<String> roles = jwt.getClaimAsMap("realm_access") != null
                ? (Collection<String>) jwt.getClaimAsMap("realm_access").get("roles")
                : Collections.emptyList();

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());

        // Quan trọng: set "principalName" theo email để sau này getMyInfo() tìm bằng email
        String email = jwt.getClaimAsString("email");

        return new JwtAuthenticationToken(jwt, authorities, email);
    }
}
