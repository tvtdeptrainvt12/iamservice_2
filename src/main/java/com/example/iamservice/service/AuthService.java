package com.example.iamservice.service;


import com.example.iamservice.dto.request.RegisterRequest;
import com.example.iamservice.entity.User;

public interface AuthService {
    User register(RegisterRequest request);
}

