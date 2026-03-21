package com.nurlansuleymanli.salonmanager.service;

import com.nurlansuleymanli.salonmanager.exception.EmailAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.PhoneNumberAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.SamePasswordException;
import com.nurlansuleymanli.salonmanager.exception.WrongPasswordException;
import com.nurlansuleymanli.salonmanager.mapper.UserMapper;
import com.nurlansuleymanli.salonmanager.model.dto.request.ChangePasswordRequest;
import com.nurlansuleymanli.salonmanager.model.dto.request.UpdateUserRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.AuthResponse;
import com.nurlansuleymanli.salonmanager.model.dto.response.UserResponse;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.repository.UserRepository;
import com.nurlansuleymanli.salonmanager.security.JwtService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService {

    UserMapper userMapper;
    UserRepository userRepository;
    JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getMyProfile(String email){
        return userMapper.toUserResponse(userRepository.findByEmail(email).get());
    }

    public AuthResponse updateMyProfile(UserEntity user, UpdateUserRequest request) {

        String newPhone = request.getPhone().trim();
        if (!user.getPhone().equals(newPhone)) {
            if (userRepository.findByPhone(newPhone).isPresent()) {
                throw new PhoneNumberAlreadyExistException("Phone number is taken!");
            }
            user.setPhone(newPhone);
        }

        String newEmail = request.getEmail().trim().toLowerCase();
        boolean isEmailChanged = false;
        
        if (!user.getEmail().equals(newEmail)) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new EmailAlreadyExistException("Email is taken!");
            }
            user.setEmail(newEmail);
            isEmailChanged = true;
        }

        user.setFullName(request.getFullName());
        userRepository.save(user);

        String newAccessToken = null;
        String newRefreshToken = null;

        if (isEmailChanged) {
            newAccessToken = jwtService.generateToken(user.getId(), user.getEmail());
            newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
        }

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }

    public UserResponse changePassword(UserEntity user, ChangePasswordRequest request){
       if(!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())){
           throw new WrongPasswordException("Password is wrong!");
       }

       if(Objects.equals(request.getOldPassword(), request.getNewPassword())){
           throw new SamePasswordException("New Password cannot be the same as Old Password!");
       }

       user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
       userRepository.save(user);

       return userMapper.toUserResponse(user);
    }

    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }
}
