package com.example.iamservice.service.impl;

import com.example.iamservice.dto.request.RegisterRequest;
import com.example.iamservice.entity.User;
import com.example.iamservice.exception.AppException;
import com.example.iamservice.exception.ErrorCode;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        return userRepository.save(user);
    }
}

