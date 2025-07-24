package com.example.iamservice.controller;

import com.example.iamservice.dto.request.ApiResponse;
import com.example.iamservice.dto.request.RegisterRequest;
import com.example.iamservice.entity.User;
import com.example.iamservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    ApiResponse<User> register(@RequestBody @Valid RegisterRequest request) {
        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setResult(authService.register(request));
        return apiResponse;
    }
}
