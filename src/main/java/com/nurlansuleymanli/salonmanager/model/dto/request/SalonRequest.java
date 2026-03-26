package com.nurlansuleymanli.salonmanager.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalonRequest {

    @NotBlank(message = "Salon name is required!")
    @Size(max = 120, message = "Salon name must be less than 120 characters")
    String name;

    @NotBlank(message = "Address is required!")
    String address;

    @NotBlank(message = "Contact phone is required!")
    @Pattern(regexp = "^\\+994(10|50|51|55|70|77|99)\\d{7}$", message = "Invalid phone format!")
    String contactPhone;

    @Email(message = "Invalid email format!")
    @NotBlank(message = "Contact email is required!")
    String contactEmail;
}
