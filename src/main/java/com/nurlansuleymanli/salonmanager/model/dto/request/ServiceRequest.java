package com.nurlansuleymanli.salonmanager.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9əöğçşiüƏÇŞİÖĞÜ\\s\\-.,]+$")
    String name;

    @NotNull
    Integer durationMin;

    @NotNull
    BigDecimal price;

    @NotNull(message = "Salon ID must be provided")
    Long salonId;

    List<Long> barberIds;
}
