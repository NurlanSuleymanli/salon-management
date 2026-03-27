package com.nurlansuleymanli.salonmanager.model.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceWithBarbersResponseDto {

    Long id;
    String name;
    Integer durationMin;
    BigDecimal price;
    List<BarberResponseDto> barbers;
}
