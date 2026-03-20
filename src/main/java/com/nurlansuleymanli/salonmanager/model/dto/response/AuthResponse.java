package com.nurlansuleymanli.salonmanager.model.dto.response;

import com.nurlansuleymanli.salonmanager.model.enums.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResponse {
    String accessToken;
    String refreshToken;
    String fullName;
    String email;
    String phone;
    Role role;
}
