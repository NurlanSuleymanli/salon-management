package com.nurlansuleymanli.salonmanager.mapper;

import com.nurlansuleymanli.salonmanager.model.dto.response.ReservationResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.ReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "barberId", source = "barber.id")
    @Mapping(target = "barberDisplayName", source = "barber.displayName")
    @Mapping(target = "salonId", source = "salon.id")
    @Mapping(target = "salonName", source = "salon.name")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    ReservationResponseDto toReservationResponseDto(ReservationEntity entity);

}
