package com.nurlansuleymanli.salonmanager.service;

import com.nurlansuleymanli.salonmanager.exception.BarberNotFoundException;
import com.nurlansuleymanli.salonmanager.mapper.BarberWorkingHourMapper;
import com.nurlansuleymanli.salonmanager.model.dto.request.BarberWorkingHourRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.BarberWorkingHourResponse;
import com.nurlansuleymanli.salonmanager.model.entity.BarberEntity;
import com.nurlansuleymanli.salonmanager.model.entity.BarberWorkingHourEntity;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.repository.BarberRepository;
import com.nurlansuleymanli.salonmanager.repository.BarberWorkingHourRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BarberWorkingHourService {

    BarberWorkingHourRepository workingHourRepository;
    BarberRepository barberRepository;
    BarberWorkingHourMapper mapper;

    public BarberWorkingHourResponse setWorkingHour(UserEntity barberUser, BarberWorkingHourRequest request) {
        BarberEntity barber = barberRepository.findByUserId(barberUser.getId())
                .orElseThrow(() -> new BarberNotFoundException("You don't have a barber profile!"));

        if (workingHourRepository.findByBarberIdAndDayOfWeek(barber.getId(), request.getDayOfWeek()).isPresent()) {
            throw new IllegalArgumentException("Working hours for " + request.getDayOfWeek() + " are already set! Use update endpoint instead.");
        }

        BarberWorkingHourEntity entity = mapper.toEntity(request);
        entity.setBarber(barber);
        
        workingHourRepository.save(entity);
        return mapper.toResponseDto(entity);
    }

    public BarberWorkingHourResponse updateWorkingHour(UserEntity barberUser, Long hourId, BarberWorkingHourRequest request) {
        BarberEntity barber = barberRepository.findByUserId(barberUser.getId())
                .orElseThrow(() -> new BarberNotFoundException("You don't have a barber profile!"));

        BarberWorkingHourEntity entity = workingHourRepository.findById(hourId)
                .orElseThrow(() -> new IllegalArgumentException("Working hour record not found!"));

        if (!entity.getBarber().getId().equals(barber.getId())) {
            throw new IllegalArgumentException("You can only modify your own working hours!");
        }

        if (!entity.getDayOfWeek().equals(request.getDayOfWeek()) && 
            workingHourRepository.findByBarberIdAndDayOfWeek(barber.getId(), request.getDayOfWeek()).isPresent()) {
            throw new IllegalArgumentException("Working hours for " + request.getDayOfWeek() + " already exist!");
        }

        mapper.updateEntityFromRequest(request, entity);
        workingHourRepository.save(entity);

        return mapper.toResponseDto(entity);
    }

    public List<BarberWorkingHourResponse> getMyWorkingHours(UserEntity barberUser) {
        BarberEntity barber = barberRepository.findByUserId(barberUser.getId())
                .orElseThrow(() -> new BarberNotFoundException("You don't have a barber profile!"));

        return workingHourRepository.findAllByBarberId(barber.getId())
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
