package com.nurlansuleymanli.salonmanager.service;


import com.nurlansuleymanli.salonmanager.mapper.UserMapper;
import com.nurlansuleymanli.salonmanager.model.dto.response.UserResponse;
import com.nurlansuleymanli.salonmanager.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService {

    UserMapper userMapper;
    UserRepository userRepository;


    public UserResponse getMyProfile(String email){
        return userMapper.toUserResponse(userRepository.findByEmail(email).get());
    }



}
