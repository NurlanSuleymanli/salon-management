package com.nurlansuleymanli.salonmanager.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9əöğçşiüƏÖĞÇŞİÜ\\s\\-\\.,]+$")
    String name;

    @NotNull
    @NumberFormat
    Integer durationMin;

    @NotNull
    BigDecimal price;

    @NotNull
    Long salonId;
    
}
