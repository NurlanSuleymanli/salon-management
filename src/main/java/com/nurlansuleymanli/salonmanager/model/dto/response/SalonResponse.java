package com.nurlansuleymanli.salonmanager.model.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalonResponse {
    Long id;
    String name;
    String address;
    String contactPhone;
    String contactEmail;
    boolean isActive;
}
