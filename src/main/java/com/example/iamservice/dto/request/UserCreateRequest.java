package com.example.iamservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    @Email(message = "INCORRECT_FORMAT")
    String email;
    @NotBlank
    String username;
    @NotBlank
    @Size(min = 3, message = "NOT_BLANK_PASSWORD")
    String password;
    LocalDate dob;
}
