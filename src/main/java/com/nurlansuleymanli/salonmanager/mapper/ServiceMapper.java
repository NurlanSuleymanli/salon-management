package com.nurlansuleymanli.salonmanager.mapper;

import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    @Mapping(target = "salonId", source = "salon.id")
    @Mapping(target = "salonName", source = "salon.name")
    ServiceResponseDto toServiceResponseDto(ServiceEntity serviceEntity);

}
