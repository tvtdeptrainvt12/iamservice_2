package com.example.iamservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_KEY(1001,"lỗi message key"),
    UNCATEGORIZED(9999, "Lỗi không xác định :>>"),
    USER_EXISTED(1002, " Email đã tồn tại"),
    INCORRECT_FORMAT(1003, "Không đúng định dạng"),
    NOT_BLANK_PASSWORD(1004, " tối thiểu ba ký tự")
    ;
    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
