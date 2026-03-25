package com.nurlansuleymanli.salonmanager.model.dto.request;

import com.nurlansuleymanli.salonmanager.model.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateReservationStatusRequest {

    @NotNull
    ReservationStatus status;

}
