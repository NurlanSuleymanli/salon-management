package com.nurlansuleymanli.salonmanager.model.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponseDto {

    String name;
    Integer durationMin;
    BigDecimal price;

    Long salonId;
    String salonName;

}
