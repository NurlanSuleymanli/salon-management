package com.nurlansuleymanli.salonmanager.service;

import com.nurlansuleymanli.salonmanager.exception.EmailAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.PhoneNumberAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.UserNotFoundException;
import com.nurlansuleymanli.salonmanager.exception.WrongPasswordException;
import com.nurlansuleymanli.salonmanager.model.enums.Role;
import com.nurlansuleymanli.salonmanager.model.dto.request.UserRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.UserResponse;
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



@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

     UserRepository userRepository;
     PasswordEncoder passwordEncoder;

    public ResponseEntity<?> registerUser(@Valid UserRequest request) {

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
                .build();

        userRepository.save(user);

        UserResponse response = UserResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


    public ResponseEntity<?> loginUser(@Valid UserRequest request){


        if (userRepository.findByEmail(request.getEmail()).isEmpty()){
            throw new UserNotFoundException("User not found!");
        }
        if (userRepository.findByPhone(request.getPhone()).isEmpty()){
            throw new UserNotFoundException("User not found!");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        if(!(passwordEncoder.matches(request.getPassword(), encodedPassword ))){
            throw new WrongPasswordException("Password is wrong!");
        }

        UserResponse response = UserResponse.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(userRepository.findByPhoneAndEmail(request.getPhone(), request.getEmail()).getRole())
                .build();

        return ResponseEntity.ok(response);

    }

}