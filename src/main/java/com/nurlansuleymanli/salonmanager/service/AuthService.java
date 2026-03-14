package com.nurlansuleymanli.salonmanager.service;

import com.nurlansuleymanli.salonmanager.exception.EmailAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.PhoneNumberAlreadyExistException;
import com.nurlansuleymanli.salonmanager.model.enums.Role;
import com.nurlansuleymanli.salonmanager.model.dto.request.RegisterUserRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.RegisterUserResponse;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

     UserRepository userRepository;
     PasswordEncoder passwordEncoder;

    public ResponseEntity<?> registerUser(@Valid RegisterUserRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new EmailAlreadyExistException("Email is taken!");
        }
        if (userRepository.findByPhone(request.getPhone()).isPresent()){
            throw new PhoneNumberAlreadyExistException("Phone number is taken!");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(encodedPassword)
                .role(Role.CUSTOMER)
                .isActive(true)
                .createdAt(Instant.from(LocalDateTime.now()))
                .updatedAt(Instant.from(LocalDateTime.now()))
                .build();

        userRepository.save(user);

        RegisterUserResponse response = RegisterUserResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}