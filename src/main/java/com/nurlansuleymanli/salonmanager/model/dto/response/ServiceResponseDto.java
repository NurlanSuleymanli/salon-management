package com.nurlansuleymanli.salonmanager.model.dto.response;

import com.nurlansuleymanli.salonmanager.model.entity.BarberEntity;
import com.nurlansuleymanli.salonmanager.model.entity.SalonEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
