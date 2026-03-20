package com.nurlansuleymanli.salonmanager.mapper;

import com.nurlansuleymanli.salonmanager.model.dto.response.UserResponse;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(UserEntity userEntity);

}
