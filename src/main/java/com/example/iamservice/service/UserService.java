package com.example.iamservice.service;

import com.cloudinary.Cloudinary;
import com.example.iamservice.constant.PredefinedRole;
import com.example.iamservice.dto.request.ChangePasswordRequest;
import com.example.iamservice.dto.request.UserCreateRequest;
import com.example.iamservice.dto.request.UserUpdateRequest;
import com.example.iamservice.dto.response.RoleResponse;
import com.example.iamservice.dto.response.UserResponse;
import com.example.iamservice.entity.Role;
import com.example.iamservice.entity.User;
import com.example.iamservice.entity.UserRole;
import com.example.iamservice.exception.AppException;
import com.example.iamservice.exception.ErrorCode;
import com.example.iamservice.mapper.UserMapper;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.repository.UserRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    UserRepository userRepository;
    Cloudinary cloudinary;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    JavaMailSender mailSender;
    StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final long OTP_EXPIRE_MINUTES = 5;


    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.USER_EXISTED);

        // map request → entity
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        // nếu không truyền role thì mặc định là USER
        Set<String> rolesToAssign = (request.getRoles() == null || request.getRoles().isEmpty())
                ? Set.of(PredefinedRole.USER_ROLE)
                : request.getRoles();

        // gán role vào bảng user_role
        rolesToAssign.forEach(roleName -> {
            roleRepository.findById(roleName).ifPresent(role -> {
                userRoleRepository.save(
                        UserRole.builder()
                                .userId(savedUser.getId())
                                .roleName(role.getName())
                                .build()
                );
            });
        });

        return buildUserResponseWithRoles(savedUser);
    }
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // xóa role cũ
        userRoleRepository.deleteByUserId(user.getId());

        // gán role mới
        request.getRoles().forEach(roleName -> {
            roleRepository.findById(roleName).ifPresent(role -> {
                userRoleRepository.save(
                        UserRole.builder()
                                .userId(user.getId())
                                .roleName(role.getName())
                                .build()
                );
            });
        });

        return buildUserResponseWithRoles(user);
    }

    private UserResponse buildUserResponseWithRoles(User user) {
        var response = userMapper.toUserResponse(user);

        var userRoles = userRoleRepository.findByUserId(user.getId());
        var roles = userRoles.stream()
                .map(ur -> roleRepository.findById(ur.getRoleName()).orElse(null))
                .filter(Objects::nonNull)
                .map(role -> new RoleResponse(role.getName(), role.getDescription(), null))
                .collect(Collectors.toSet());

        response.setRoles(roles);
        return response;
    }

    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByEmail(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return buildUserResponseWithRoles(user);
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll()
                .stream()
                .map(this::buildUserResponseWithRoles)
                .toList();
    }

    @PostAuthorize("returnObject.email == authentication.name")
    public UserResponse getUser(String id) {
        log.info("In method get user by id");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildUserResponseWithRoles(user);
    }

    public void changePassword(String email, ChangePasswordRequest request){
        var user = userRepository.findByEmail(email)
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_EXISTED));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    public void sendOtp(String email){
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ko ton tai"));

        String otp = String.format("%06d",new Random().nextInt(999999));

        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otp,
                OTP_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã OTP đặt lại mật khẩu");
        message.setText("Mã OTP của bạn là: " + otp + " (hết hạn sau 5 phút)");
        mailSender.send(message);
    }
    public void verifyOtpAndChangePassword(String email, String otp, String newPassword) {
        String key = OTP_PREFIX + email;
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) {
            throw new RuntimeException("OTP đã hết hạn hoặc chưa được gửi");
        }
        if (!savedOtp.equals(otp)) {
            throw new RuntimeException("OTP không hợp lệ");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisTemplate.delete(key);
    }

    public void uploadAvatar(MultipartFile file, String email) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), Map.of());
        String imageUrl = (String) result.get("secure_url");

        user.setAvatar(imageUrl);
        userRepository.save(user);
    }
    // xóa mềm chỉ hind nó thôi không phải xóa vĩnh viễn
    public void deleteSoft(String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));
        userRepository.delete(user);
    }
    public void lockUser(String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));
        user.setBlock(true);
        userRepository.save(user);
    }
    public void unlockUser(String userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("user not found"));
        user.setBlock(false);
        userRepository.save(user);
    }
    //tìm kiếm và phân trang
    public Page<UserResponse> getUser(String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page , size, Sort.by("email").ascending());

        Specification<User> spec = (root, query, cb) -> {
            if (keyword != null && !keyword.isEmpty()){
                String likePattern = "%" + keyword.toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("email")), likePattern),
                        cb.like(cb.lower(root.get("username")), likePattern));
            }
            return cb.conjunction();
        };
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::buildUserResponseWithRoles);
    }
}