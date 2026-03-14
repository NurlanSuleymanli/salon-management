package com.nurlansuleymanli.salonmanager.model.dto.response;

import com.nurlansuleymanli.salonmanager.model.enums.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RegisterUserResponse {


    String fullName;
    String email;
    String phone;
    Role role;
}
