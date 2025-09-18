package com.example.iamservice.config;

import com.example.iamservice.constant.PredefinedRole;
import com.example.iamservice.entity.Role;
import com.example.iamservice.entity.User;
import com.example.iamservice.entity.UserRole;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.repository.UserRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "org.postgresql.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        UserRoleRepository userRoleRepository) {
        log.info("Initializing application.....");
        return args -> {
            // Đảm bảo luôn có ROLE_USER
            roleRepository.findById(PredefinedRole.USER_ROLE)
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name(PredefinedRole.USER_ROLE)
                                    .description("User role")
                                    .build()
                    ));

            // Đảm bảo luôn có ROLE_ADMIN
            Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE)
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name(PredefinedRole.ADMIN_ROLE)
                                    .description("Admin role")
                                    .build()
                    ));

            // Tạo user admin mặc định nếu chưa có
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .build();
                userRepository.save(user);

                userRoleRepository.save(
                        UserRole.builder()
                                .userId(user.getId())
                                .roleName(adminRole.getName())
                                .build()
                );

                log.warn("admin user has been created with default password: admin, please change it");
            }

            log.info("Application initialization completed .....");
        };

    }
}