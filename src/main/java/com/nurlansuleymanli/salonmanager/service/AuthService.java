package com.nurlansuleymanli.salonmanager.service;

import com.nurlansuleymanli.salonmanager.exception.EmailAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.PhoneNumberAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.UserNotFoundException;
import com.nurlansuleymanli.salonmanager.model.enums.Role;
import com.nurlansuleymanli.salonmanager.model.dto.request.UserRequest;
import com.nurlansuleymanli.salonmanager.model.dto.request.LoginRequest;
import com.nurlansuleymanli.salonmanager.model.dto.request.RefreshTokenRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.AuthResponse;
import com.nurlansuleymanli.salonmanager.model.entity.TokenBlacklistEntity;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.repository.TokenBlacklistRepository;
import com.nurlansuleymanli.salonmanager.repository.UserRepository;
import com.nurlansuleymanli.salonmanager.security.JwtService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.time.ZoneId;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserRepository userRepository;
    TokenBlacklistRepository tokenBlacklistRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    AuthenticationManager authenticationManager;

    public ResponseEntity<?> registerUser(@Valid UserRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Registration failed: Email {} is already taken", email);
            throw new EmailAlreadyExistException("Email is taken!");
        }
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            log.warn("Registration failed: Phone number {} is already taken", request.getPhone());
            throw new PhoneNumberAlreadyExistException("Phone number is taken!");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .email(email)
                .phone(request.getPhone())
                .passwordHash(encodedPassword)
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();

        log.info("New user registered successfully: {} (ID: {})", user.getEmail(), user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    public ResponseEntity<?> loginUser(@Valid LoginRequest request) {
        
        String email = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        String jwtToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        log.info("User logged in successfully: {} (ID: {})", user.getEmail(), user.getId());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> refreshToken(@Valid RefreshTokenRequest request) {
        String reqRefreshToken = request.getRefreshToken();

        if (!jwtService.isValid(reqRefreshToken)) {
            log.warn("Refresh token attempt failed: token is expired or invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh token is expired!"));
        }

        String email = jwtService.extractEmail(reqRefreshToken);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        log.info("Tokens refreshed successfully for user: {} (ID: {})", user.getEmail(), user.getId());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> logoutUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            if (!tokenBlacklistRepository.existsByToken(jwt) && jwtService.isValid(jwt)) {
                TokenBlacklistEntity blacklistEntity = TokenBlacklistEntity.builder()
                        .token(jwt)
                        .expiresAt(jwtService.extractExpiration(jwt).toInstant())
                        .build();

                tokenBlacklistRepository.save(blacklistEntity);
                log.info("Token explicitly blacklisted (Logged Out): {}", jwt.substring(0, 15) + "...");
            }
        }
        return ResponseEntity.ok(Map.of("message", "Successfully logged out!"));
    }
}