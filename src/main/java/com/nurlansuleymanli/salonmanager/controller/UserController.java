package com.nurlansuleymanli.salonmanager.controller;

import com.nurlansuleymanli.salonmanager.model.dto.request.ChangePasswordRequest;
import com.nurlansuleymanli.salonmanager.model.dto.request.UpdateUserRequest;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PutMapping("/update")
    public ResponseEntity<?> updateMyProfile(@AuthenticationPrincipal UserEntity user,
                                             @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(user, request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserEntity user,
                                            @RequestBody @Valid ChangePasswordRequest request){

        return ResponseEntity.ok(userService.changePassword(user, request));
    }
}
