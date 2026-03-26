package com.nurlansuleymanli.salonmanager.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarberWorkingHourRequest {

    @NotNull(message = "Day of week is required!")
    DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required!")
    LocalTime startTime;

    @NotNull(message = "End time is required!")
    LocalTime endTime;

    LocalTime breakStartTime;
    LocalTime breakEndTime;
}
