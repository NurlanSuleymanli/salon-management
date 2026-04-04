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
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
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

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeUserStatus(@PathVariable Long id){
        return ResponseEntity.ok(userService.changeUserStatus(id));
    }

    @PutMapping("/{id}/make-admin")
    public ResponseEntity<?> makeAdmin(@PathVariable Long id){
        return ResponseEntity.ok(userService.makeAdmin(id));
    }
}
