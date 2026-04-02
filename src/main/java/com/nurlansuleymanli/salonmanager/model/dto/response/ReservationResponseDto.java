package com.nurlansuleymanli.salonmanager.model.dto.response;

import com.nurlansuleymanli.salonmanager.model.enums.ReservationStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
    List<Long> serviceIds;
    List<String> serviceNames;
    Integer totalDurationMin;
    BigDecimal totalPrice;
    Instant startAt;
    Instant endAt;
    ReservationStatus status;
    Instant cancelledAt;
    String cancelReason;

}
