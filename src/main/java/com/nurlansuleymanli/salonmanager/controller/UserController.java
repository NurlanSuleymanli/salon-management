package com.nurlansuleymanli.salonmanager.controller;


import com.nurlansuleymanli.salonmanager.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserController {

    UserService userService;


    @GetMapping("/current")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserDetails userDetails){

        String myEmail= userDetails.getUsername();

        return ResponseEntity.ok(userService.getMyProfile(myEmail));


    }


}
