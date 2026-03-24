package com.nurlansuleymanli.salonmanager.model.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarberResponseDto {

    Long id;
    Long userId;
    Long salonId;
    String salonName;
    String displayName;
    List<ServiceResponseDto> services;
    boolean isActive;

}
