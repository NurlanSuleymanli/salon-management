package com.nurlansuleymanli.salonmanager.service;


import com.nurlansuleymanli.salonmanager.exception.NoAvailableSalonException;
import com.nurlansuleymanli.salonmanager.exception.ServiceAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.ServiceNotFoundException;
import com.nurlansuleymanli.salonmanager.exception.BarberNotFoundException;
import com.nurlansuleymanli.salonmanager.mapper.ServiceMapper;
import com.nurlansuleymanli.salonmanager.model.dto.request.ServiceRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceWithBarbersResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.BarberEntity;
import com.nurlansuleymanli.salonmanager.model.entity.SalonEntity;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.repository.BarberRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.nurlansuleymanli.salonmanager.mapper.BarberMapper;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ServicesService {

    ServiceRepository serviceRepository;
    SalonRepository salonRepository;
    BarberRepository barberRepository;
    ServiceMapper serviceMapper;
    BarberMapper barberMapper;


    public Page<ServiceResponseDto> getServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceEntity> entityPage = serviceRepository.findAllByIsActiveTrue(pageable);
        return entityPage.map(serviceMapper::toServiceResponseDto);
    }

    public List<ServiceWithBarbersResponseDto> getServicesWithBarbersBySalonId(Long salonId) {
        List<ServiceEntity> services = serviceRepository.findAllBySalonIdAndIsActiveTrue(salonId);
        
        return services.stream().map(service -> 
            ServiceWithBarbersResponseDto.builder()
                .id(service.getId())
                .name(service.getName())
                .durationMin(service.getDurationMin())
                .price(service.getPrice())
                .barbers(service.getBarbers().stream()
                        .filter(BarberEntity::isActive)
                        .map(barberMapper::toBarberResponseDto)
                        .collect(Collectors.toList()))
                .build()
        ).collect(Collectors.toList());
    }

    public List<ServiceResponseDto> getServicesByBarberSalon(UserEntity user) {
        BarberEntity barber = barberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BarberNotFoundException("Barber profile not found!"));
        List<ServiceEntity> services = serviceRepository.findAllBySalonIdAndIsActiveTrue(barber.getSalon().getId());
        return services.stream().map(serviceMapper::toServiceResponseDto).collect(Collectors.toList());
    }

    public ServiceResponseDto createService(ServiceRequest request, UserEntity caller) {

        if(serviceRepository.findByName(request.getName()).isPresent()){
            throw new ServiceAlreadyExistException("Service already exist!");
        }

        ServiceEntity newService = serviceMapper.toServiceEntity(request);

        SalonEntity salon = salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new NoAvailableSalonException("Salon not available!"));

        newService.setSalon(salon);
        serviceRepository.save(newService);

        if (request.getBarberIds() != null && !request.getBarberIds().isEmpty()) {
            List<BarberEntity> selectedBarbers = barberRepository.findAllById(request.getBarberIds());
            for (BarberEntity b : selectedBarbers) {
                b.getServices().add(newService);
                barberRepository.save(b);
            }
        } else if (caller != null) {
            barberRepository.findByUserId(caller.getId()).ifPresent(barber -> {
                barber.getServices().add(newService);
                barberRepository.save(barber);
            });
        }

        return serviceMapper.toServiceResponseDto(newService);
    }


    public ServiceResponseDto updateService(ServiceRequest request, Long id){

        ServiceEntity serviceEntity = serviceRepository.findById(id)
                .orElseThrow(()-> new ServiceNotFoundException("Service not found!"));

        serviceRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ServiceAlreadyExistException("Service already exist!");
            }
        });

        SalonEntity salon = salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new NoAvailableSalonException("Salon not available!"));

        serviceEntity.setSalon(salon);
        serviceEntity.setName(request.getName());
        serviceEntity.setPrice(request.getPrice());
        serviceEntity.setDurationMin(request.getDurationMin());

        // Update barber links
        if (request.getBarberIds() != null) {
            // Remove from current barbers
            List<BarberEntity> currentBarbers = barberRepository.findAllByServicesId(serviceEntity.getId());
            for (BarberEntity b : currentBarbers) {
                b.getServices().removeIf(s -> s.getId().equals(serviceEntity.getId()));
                barberRepository.save(b);
            }
            
            // Add to new barbers
            if (!request.getBarberIds().isEmpty()) {
                List<BarberEntity> newBarbers = barberRepository.findAllById(request.getBarberIds());
                for (BarberEntity b : newBarbers) {
                    b.getServices().add(serviceEntity);
                    barberRepository.save(b);
                }
            }
        }

        serviceRepository.save(serviceEntity);

        return serviceMapper.toServiceResponseDto(serviceEntity);

    }

    public ServiceResponseDto deleteService(Long id){

        Optional<ServiceEntity> serviceEntity = serviceRepository.findById(id);

        if(serviceEntity.isEmpty()){
            throw new ServiceNotFoundException("Service not found!");
        }

        serviceEntity.get().setActive(false);
        serviceRepository.save(serviceEntity.get());

        return serviceMapper.toServiceResponseDto(serviceEntity.get());



    }

}
