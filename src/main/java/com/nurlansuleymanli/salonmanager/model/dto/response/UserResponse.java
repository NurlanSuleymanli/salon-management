package com.nurlansuleymanli.salonmanager.model.dto.response;


import com.nurlansuleymanli.salonmanager.model.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String fullName;
    private String email;
    private String phone;
    private Role role;
}
