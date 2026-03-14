package com.nurlansuleymanli.salonmanager.model.dto.request;


import com.nurlansuleymanli.salonmanager.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RegisterUserRequest {
    @NotBlank
    @Size(max = 120)
    String fullName;

    @NotBlank
    @Email
    @Size(max = 190)
    String email;

    @NotBlank
    @Size(max = 32)
    @Pattern(regexp = "^\\+994(10|50|51|55|70|77|99)\\d{7}$")
    String phone;

    @NotBlank
    @Size(max = 255)
    String password;

    @NotBlank
    Role role;
}
