package com.example.iamservice.service;

import com.example.iamservice.constant.PredefinedRole;
import com.example.iamservice.entity.User;
import com.example.iamservice.entity.UserRole;
import com.example.iamservice.exception.AppException;
import com.example.iamservice.exception.ErrorCode;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public String handleGoogleLogin(OidcUser principal) {
        String email = principal.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(principal.getFullName());
                    newUser.setPassword(UUID.randomUUID().toString());
                    User savedUser = userRepository.save(newUser);

                    // luôn gán role USER cho tài khoản Google
                    roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(role -> {
                        userRoleRepository.save(
                                UserRole.builder()
                                        .userId(savedUser.getId())
                                        .roleName(role.getName())
                                        .build()
                        );
                    });

                    return savedUser;
                });
        return authService.generateToken(user);
    }
}
