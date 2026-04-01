package com.nurlansuleymanli.salonmanager.mapper;

import com.nurlansuleymanli.salonmanager.model.dto.response.BarberResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.BarberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ServiceMapper.class})
public interface BarberMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "salonId", source = "salon.id")
    @Mapping(target = "salonName", source = "salon.name")
    @Mapping(target = "isActive", source = "active")
    BarberResponseDto toBarberResponseDto(BarberEntity barberEntity);

}
