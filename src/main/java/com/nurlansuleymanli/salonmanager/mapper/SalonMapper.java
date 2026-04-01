package com.nurlansuleymanli.salonmanager.mapper;

import com.nurlansuleymanli.salonmanager.model.dto.request.SalonRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.SalonResponse;
import com.nurlansuleymanli.salonmanager.model.entity.SalonEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SalonMapper {

    SalonEntity toSalonEntity(SalonRequest request);


    @Mapping(target = "isActive", source = "active")
    SalonResponse toSalonResponse(SalonEntity entity);



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salonWorkingHours", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(SalonRequest request, @MappingTarget SalonEntity entity);
}
