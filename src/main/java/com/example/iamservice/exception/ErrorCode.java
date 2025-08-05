package com.example.iamservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    INVALID_KEY(1001,"lỗi message key", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED(9999, "Lỗi không xác định :>>",  HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, " Email đã tồn tại", HttpStatus.BAD_REQUEST),
    INCORRECT_FORMAT(1003, "Không đúng định dạng", HttpStatus.BAD_REQUEST),
    NOT_BLANK_PASSWORD(1004, " tối thiểu ba ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, " Email không tồn tại", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "authenticated", HttpStatus.UNAUTHORIZED)
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
