package com.nurlansuleymanli.salonmanager.controller;


import com.nurlansuleymanli.salonmanager.model.dto.request.RegisterUserRequest;
import com.nurlansuleymanli.salonmanager.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

     AuthService authService;


        @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request){
        return authService.registerUser(request);
    }



}
