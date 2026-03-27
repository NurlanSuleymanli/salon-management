package com.nurlansuleymanli.salonmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        log.info("Incoming Request: {} {} - Client IP: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                request.getRemoteAddr());

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (!request.getRequestURI().contains("swagger") && !request.getRequestURI().contains("api-docs")) {
                if (response.getStatus() >= 400) {
                    log.warn("Failed Response: {} {} - Status: {} - Duration: {}ms", 
                            request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
                } else {
                    log.info("Success Response: {} {} - Status: {} - Duration: {}ms", 
                            request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
                }
            }
        }
    }
}
