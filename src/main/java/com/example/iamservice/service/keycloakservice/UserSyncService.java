package com.example.iamservice.service.keycloakservice;

import com.example.iamservice.constant.PredefinedRole;
import com.example.iamservice.entity.User;
import com.example.iamservice.entity.UserRole;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSyncService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public User syncUserFromToken(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .password("N/A")
                    .build();
            return userRepository.save(newUser);
        });

        // Gán role USER nếu user chưa có
        boolean hasUserRole = userRoleRepository.existsByUserIdAndRoleName(user.getId(), PredefinedRole.USER_ROLE);
        if (!hasUserRole) {
            roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(role -> {
                userRoleRepository.save(
                        UserRole.builder()
                                .userId(user.getId())
                                .roleName(role.getName())
                                .build()
                );
            });
        }

        // Bổ sung thông tin nếu thiếu
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(username);
        }

        return user;
    }
}
