package com.nurlansuleymanli.salonmanager.mapper;

import com.nurlansuleymanli.salonmanager.model.dto.response.ReservationResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.ReservationEntity;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {Collectors.class, ServiceEntity.class, BigDecimal.class})
public interface ReservationMapper {

    @Mapping(target = "customerId",        source = "customer.id")
    @Mapping(target = "customerName",      source = "customer.fullName")
    @Mapping(target = "barberId",          source = "barber.id")
    @Mapping(target = "barberDisplayName", source = "barber.displayName")
    @Mapping(target = "salonId",           source = "salon.id")
    @Mapping(target = "salonName",         source = "salon.name")
    @Mapping(target = "serviceIds",
            expression = "java(entity.getServices() != null ? entity.getServices().stream()." +
                    "map(ServiceEntity::getId).collect(Collectors.toList()) : java.util.Collections.emptyList())")
    @Mapping(target = "serviceNames",
             expression = "java(entity.getServices().stream().map(ServiceEntity::getName).collect(Collectors.toList()))")
    @Mapping(target = "totalDurationMin",
             expression = "java(entity.getServices().stream().mapToInt(ServiceEntity::getDurationMin).sum())")
    @Mapping(target = "totalPrice",
             expression = "java(entity.getServices().stream().map(ServiceEntity::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add))")
    ReservationResponseDto toReservationResponseDto(ReservationEntity entity);

}
