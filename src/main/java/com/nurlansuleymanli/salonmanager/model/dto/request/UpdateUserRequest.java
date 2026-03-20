package com.nurlansuleymanli.salonmanager.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank
    @Size(max = 120)
    String fullName;

    @NotBlank
    @Size(max = 32)
    @Pattern(regexp = "^\\+994(10|50|51|55|70|77|99)\\d{7}$")
    String phone;

    @NotBlank
    @Email
    @Size(max = 190)
    String email;
}
