package com.nurlansuleymanli.salonmanager.service;


import com.nurlansuleymanli.salonmanager.exception.NoAvailableSalonException;
import com.nurlansuleymanli.salonmanager.exception.ServiceAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.ServiceNotFoundException;
import com.nurlansuleymanli.salonmanager.mapper.ServiceMapper;
import com.nurlansuleymanli.salonmanager.model.dto.request.ServiceRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.SalonEntity;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import com.nurlansuleymanli.salonmanager.repository.SalonRepository;
import com.nurlansuleymanli.salonmanager.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ServicesService {

    ServiceRepository serviceRepository;
    SalonRepository salonRepository;
    ServiceMapper serviceMapper;


    public Page<ServiceResponseDto> getServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceEntity> entityPage = serviceRepository.findAll(pageable);
        return entityPage.map(serviceMapper::toServiceResponseDto);
    }

    public ServiceResponseDto createService(ServiceRequest request){

        if(serviceRepository.findByName(request.getName()).isPresent()){
            throw new ServiceAlreadyExistException("Service already exist!");
        }

        ServiceEntity newService = serviceMapper.toServiceEntity(request);

        SalonEntity salon = salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new NoAvailableSalonException("Salon not available!"));

        newService.setSalon(salon);

        serviceRepository.save(newService);

        return serviceMapper.toServiceResponseDto(newService);
    }


    public ServiceResponseDto updateService(ServiceRequest request, Long id){

        if(serviceRepository.findById(id).isEmpty()){
            throw new ServiceNotFoundException("Service not found!");
        }

        ServiceEntity changedService = serviceMapper.toServiceEntity(request);

        SalonEntity salon = salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new NoAvailableSalonException("Salon not available!"));

        changedService.setSalon(salon);

        serviceRepository.save(changedService);

        return serviceMapper.toServiceResponseDto(changedService);

    }

    public ServiceResponseDto deleteService(Long id){

        Optional<ServiceEntity> serviceEntity = serviceRepository.findById(id);

        if(serviceEntity.isEmpty()){
            throw new ServiceNotFoundException("Service not found!");
        }

        serviceRepository.deleteById(id);

        return serviceMapper.toServiceResponseDto(serviceEntity.get());



    }

}
