package com.nurlansuleymanli.salonmanager.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarberRequest {

    @NotNull
    Long userId;

    @NotNull
    Long salonId;

    @NotBlank
    @Size(min = 2, max = 100)
    String displayName;

    List<Long> serviceIds;

}
