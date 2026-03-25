package com.nurlansuleymanli.salonmanager.model.dto.response;

import com.nurlansuleymanli.salonmanager.model.enums.ReservationStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationResponseDto {

    Long id;
    Long customerId;
    String customerName;
    Long barberId;
    String barberDisplayName;
    Long salonId;
    String salonName;
    Long serviceId;
    String serviceName;
    Instant startAt;
    Instant endAt;
    ReservationStatus status;
    Instant cancelledAt;
    String cancelReason;

}
