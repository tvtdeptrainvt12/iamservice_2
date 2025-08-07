package com.example.iamservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.iamservice.dto.request.ChangePasswordRequest;
import com.example.iamservice.dto.request.UserCreateRequest;
import com.example.iamservice.dto.request.UserUpdateRequest;
import com.example.iamservice.dto.response.UserResponse;
import com.example.iamservice.entity.User;
import com.example.iamservice.exception.AppException;
import com.example.iamservice.exception.ErrorCode;
import com.example.iamservice.mapper.UserMapper;
import com.example.iamservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    Cloudinary cloudinary;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    JavaMailSender mailSender;
    StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final long OTP_EXPIRE_MINUTES = 5;


    public User createUser(UserCreateRequest request) {

        if(userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.UpdateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")));
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
}