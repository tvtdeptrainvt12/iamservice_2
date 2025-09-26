package com.example.iamservice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleController {
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal OidcUser principal){
        return "xin chao" + principal.getFullName() +
                " (email: " + principal.getEmail() + ")";
    }
}
