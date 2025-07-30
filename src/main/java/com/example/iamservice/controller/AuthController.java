package com.example.iamservice.controller;

import com.example.iamservice.dto.request.ApiResponse;
import com.example.iamservice.dto.request.AuthRequest;
import com.example.iamservice.dto.response.AuthResponse;
import com.example.iamservice.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/log-in")
    ApiResponse<AuthResponse> authenticate(@RequestBody AuthRequest request){
        boolean result = authService.authenticate(request);
        return ApiResponse.<AuthResponse>builder()
                .result(AuthResponse.builder()
                        .authenticated(result)
                        .build())
                .build();
    }
}
