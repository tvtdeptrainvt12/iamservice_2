package com.example.iamservice.filter;

import com.example.iamservice.service.ApiLogService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Order(1)
public class RequestResponseDbLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseDbLoggingFilter.class);

    private final ApiLogService apiLogService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper((HttpServletResponse) response);

        String username = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "anonymous";

        Exception exception = null;

        try{
            chain.doFilter(req, res);
        } catch (Exception ex){
            exception =ex;
            throw ex;
        }finally {
            String requestBody = new String(req.getContentAsByteArray());
            String responseBody =  new String(res.getContentAsByteArray());

            apiLogService.saveLog(
                    username,
                    req.getMethod(),
                    req.getRequestURI(),
                    requestBody,
                    responseBody,
                    res.getStatus(),
                    exception != null ? exception.getMessage() : null
            );
            res.copyBodyToResponse();
        }
    }
}
