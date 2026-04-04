package com.nurlansuleymanli.salonmanager.mapper;

import com.nurlansuleymanli.salonmanager.model.dto.request.ServiceRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.stream.Collectors;
import com.nurlansuleymanli.salonmanager.model.entity.BarberEntity;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    @Mapping(target = "salonId", source = "salon.id")
    @Mapping(target = "salonName", source = "salon.name")
    @Mapping(target = "barberIds", expression = "java(mapBarbers(serviceEntity.getBarbers()))")
    ServiceResponseDto toServiceResponseDto(ServiceEntity serviceEntity);

    @Mapping(target = "salon.id", source = "salonId")
    ServiceEntity toServiceEntity(ServiceRequest serviceRequest);

    default List<Long> mapBarbers(List<BarberEntity> barbers) {
        if (barbers == null) return null;
        return barbers.stream().map(BarberEntity::getId).collect(Collectors.toList());
    }
}
