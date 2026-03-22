package com.nurlansuleymanli.salonmanager.service;


import com.nurlansuleymanli.salonmanager.mapper.ServiceMapper;
import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import com.nurlansuleymanli.salonmanager.repository.ServiceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ServicesService {

    ServiceRepository serviceRepository;
    ServiceMapper serviceMapper;


    public Page<ServiceResponseDto> getServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceEntity> entityPage = serviceRepository.findAll(pageable);
        return entityPage.map(serviceMapper::toServiceResponseDto);
    }

}
