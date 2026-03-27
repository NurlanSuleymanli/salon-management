package com.nurlansuleymanli.salonmanager.service;

import com.nurlansuleymanli.salonmanager.mapper.SalonMapper;
import com.nurlansuleymanli.salonmanager.model.dto.request.SalonRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.SalonResponse;
import com.nurlansuleymanli.salonmanager.model.entity.SalonEntity;
import com.nurlansuleymanli.salonmanager.repository.SalonRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SalonService {

    SalonRepository salonRepository;
    SalonMapper salonMapper;

    public SalonResponse createSalon(SalonRequest request) {
        if (salonRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("A salon with this name already exists!");
        }

        SalonEntity salon = salonMapper.toSalonEntity(request);
        salon.setActive(true);
        salonRepository.save(salon);

        return salonMapper.toSalonResponseDto(salon);
    }

    public SalonResponse updateSalon(Long id, SalonRequest request) {
        SalonEntity salon = salonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Salon not found!"));

        if (!salon.getName().equals(request.getName()) && 
            salonRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("A salon with this name already exists!");
        }

        salonMapper.updateEntityFromRequest(request, salon);
        salonRepository.save(salon);

        return salonMapper.toSalonResponseDto(salon);
    }

    public SalonResponse deleteSalon(Long id) {
        SalonEntity salon = salonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Salon not found!"));
        
        salon.setActive(false);
        salonRepository.save(salon);

        return salonMapper.toSalonResponseDto(salon);
    }

    public Page<SalonResponse> getSalons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return salonRepository.findAllByIsActiveTrue(pageable)
                .map(salonMapper::toSalonResponseDto);
    }

    public SalonResponse getSalonById(Long id) {
        SalonEntity salon = salonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Salon not found!"));
        return salonMapper.toSalonResponseDto(salon);
    }
}
