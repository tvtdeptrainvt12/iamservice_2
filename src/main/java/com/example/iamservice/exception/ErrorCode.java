package com.example.iamservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001,"lỗi message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, " Email đã tồn tại", HttpStatus.BAD_REQUEST),
    INCORRECT_FORMAT(1003, "Không đúng định dạng", HttpStatus.BAD_REQUEST),
    NOT_BLANK_PASSWORD(1004, " tối thiểu ba ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, " Email không tồn tại", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "authenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    USER_BLOCK(1009,"tài khoản bị khóa",HttpStatus.BAD_REQUEST),
    ROLE_USER_NOT_FOUND(1010,"cho em một danh phận",HttpStatus.BAD_REQUEST)
    ;
    private int code;
    private String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
