package com.nurlansuleymanli.salonmanager.mapper;

import com.nurlansuleymanli.salonmanager.model.dto.request.BarberWorkingHourRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.BarberWorkingHourResponse;
import com.nurlansuleymanli.salonmanager.model.entity.BarberWorkingHourEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BarberWorkingHourMapper {

    BarberWorkingHourEntity toEntity(BarberWorkingHourRequest request);

    @Mapping(target = "barberId", source = "barber.id")
    BarberWorkingHourResponse toResponseDto(BarberWorkingHourEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "barber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(BarberWorkingHourRequest request, @MappingTarget BarberWorkingHourEntity entity);
}
