package com.example.iamservice.service;

import com.example.iamservice.entity.ApiLog;
import com.example.iamservice.repository.ApiLogRepository;
import com.example.iamservice.utils.SensitiveDataUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApiLogService {

    private final ApiLogRepository apiLogRepository;

    public void saveLog(String username, String method ,String uri,
                        String requestBody, String responseBody,
                        Integer statusCode, String exception){
        ApiLog log = ApiLog.builder()
                .username(username)
                .method(method)
                .uri(uri)
                .requestBody(SensitiveDataUtils.maskPassword(requestBody))
                .responseBody(responseBody)
                .statusCode(statusCode)
                .exception(exception)
                .createdAt(LocalDateTime.now())
                .build();

        apiLogRepository.save(log);
    }
}
