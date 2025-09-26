package com.example.iamservice.security;

import com.example.iamservice.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleAuthService googleAuthService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OidcUser principal = (OidcUser) authentication.getPrincipal();

            String token = googleAuthService.handleGoogleLogin(principal);

            response.setContentType("application/json");
            response.getWriter().write("{\"token\":\"" + token + "\"}");
        } catch (Exception e) {
            e.printStackTrace(); // log ra console
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Login with Google failed: " + e.getMessage());
        }
    }
}