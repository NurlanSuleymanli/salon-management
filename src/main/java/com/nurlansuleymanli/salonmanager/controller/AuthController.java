package com.nurlansuleymanli.salonmanager.controller;


import com.nurlansuleymanli.salonmanager.model.dto.request.UserRequest;
import com.nurlansuleymanli.salonmanager.model.dto.request.LoginRequest;
import com.nurlansuleymanli.salonmanager.model.dto.request.RefreshTokenRequest;
import com.nurlansuleymanli.salonmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

     AuthService authService;
     Map<String, Bucket> cache = new ConcurrentHashMap<>();

     private Bucket resolveBucket(String ip) {
         return cache.computeIfAbsent(ip, k -> Bucket.builder()
                 .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                 .build());
     }

     private String getClientIP(HttpServletRequest request) {
         String ipInfo = request.getHeader("X-Forwarded-For");
         if (ipInfo == null || ipInfo.isEmpty() || "unknown".equalsIgnoreCase(ipInfo)) {
             return request.getRemoteAddr();
         }
         return ipInfo.split(",")[0].trim();
     }

     @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRequest request, HttpServletRequest httpRequest){
        Bucket bucket = resolveBucket(getClientIP(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many requests. Please wait 1 minute."));
        }
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest){
        Bucket bucket = resolveBucket(getClientIP(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many requests. Please wait 1 minute."));
        }
         return authService.loginUser(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest request){
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader){
        return authService.logoutUser(authHeader);
    }

}
