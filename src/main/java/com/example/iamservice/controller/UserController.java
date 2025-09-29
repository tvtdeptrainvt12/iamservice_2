package com.example.iamservice.controller;

import com.example.iamservice.dto.request.ApiResponse;
import com.example.iamservice.dto.request.ChangePasswordRequest;
import com.example.iamservice.dto.request.UserCreateRequest;
import com.example.iamservice.dto.request.UserUpdateRequest;
import com.example.iamservice.dto.response.UserResponse;
import com.example.iamservice.entity.User;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    UserRepository userRepository;

    @PostMapping
    //@PreAuthorize("hasPermission('ROLE_ADMIN', 'CREATE_DATA')")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasPermission('ROLE_ADMIN', 'READ_DATA')")
    ApiResponse<List<UserResponse>> getUsers(){

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Email: {}",authentication.getName());
        authentication.getAuthorities()
                .forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/myInfo")
    @PreAuthorize("hasPermission('ROLE_USER', 'READ_DATA')")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasPermission('ROLE_USER', 'UPDATE_DATA')")
    UserResponse updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request){
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return "User has been deleted";
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteSoft(@PathVariable String id){
        userService.deleteSoft(id);
        return ResponseEntity.ok("user soft delete");
    }
    @PostMapping("/change-password")
    @PreAuthorize("hasPermission('ROLE_USER', 'UPDATE_DATA')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            Authentication authentication){
        String email = authentication.getName();
        userService.changePassword(email, request);
        return ResponseEntity.ok("Password changed successfully");
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        userService.sendOtp(email);
        return ResponseEntity.ok("OTP đã được gửi đến email của bạn");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        userService.verifyOtpAndChangePassword(email, otp, newPassword);
        return ResponseEntity.ok("Mật khẩu đã được đổi thành công");
    }
    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.uploadAvatar(file, email);
        return ResponseEntity.ok("Upload successful");
    }
    @PostMapping("/{id}/block")
    @PreAuthorize("hasPermission('ROLE_USER', 'REJECT_POST')")
    public ApiResponse<String> blockUser(@PathVariable String id){
        userService.lockUser(id);
        return ApiResponse.<String>builder()
                .result("user da bi khoa")
                .build();
    }
    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasPermission('ROLE_USER', 'APPROVE_POST')")
    public ApiResponse<String> unlockUser(@PathVariable String id){
        userService.unlockUser(id);
        return ApiResponse.<String>builder()
                .result("user da mo khoa")
                .build();
    }
    @GetMapping("/search")
    //@PreAuthorize("hasPermission('ROLE_ADMIN', 'READ_DATA')")
    public Page<UserResponse> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        return userService.getUser(keyword,page ,size);
    }
}
