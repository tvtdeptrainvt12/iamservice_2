package com.example.iamservice.dto.request;

import com.example.iamservice.exception.ErrorCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "INCORRECT_FORMAT")
    @NotBlank
    private String email;
    @NotBlank(message = "NOT_BLANK_PASSWORD")
    private String password;
}

