package com.nurlansuleymanli.salonmanager.service;

import com.nurlansuleymanli.salonmanager.exception.AdminCannotBeBarberException;
import com.nurlansuleymanli.salonmanager.exception.BarberAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.BarberNotFoundException;
import com.nurlansuleymanli.salonmanager.exception.NoAvailableSalonException;
import com.nurlansuleymanli.salonmanager.exception.UserNotFoundException;
import com.nurlansuleymanli.salonmanager.model.enums.ReservationStatus;
import com.nurlansuleymanli.salonmanager.model.enums.Role;
import com.nurlansuleymanli.salonmanager.mapper.BarberMapper;
import com.nurlansuleymanli.salonmanager.mapper.ServiceMapper;
import com.nurlansuleymanli.salonmanager.model.dto.request.BarberRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.BarberResponseDto;
import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.BarberEntity;
import com.nurlansuleymanli.salonmanager.model.entity.BarberWorkingHourEntity;
import com.nurlansuleymanli.salonmanager.model.entity.ReservationEntity;
import com.nurlansuleymanli.salonmanager.model.entity.SalonEntity;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.repository.BarberRepository;
import com.nurlansuleymanli.salonmanager.repository.BarberWorkingHourRepository;
import com.nurlansuleymanli.salonmanager.repository.ReservationRepository;
import com.nurlansuleymanli.salonmanager.repository.SalonRepository;
import com.nurlansuleymanli.salonmanager.repository.ServiceRepository;
import com.nurlansuleymanli.salonmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BarberService {

    BarberRepository barberRepository;
    UserRepository userRepository;
    SalonRepository salonRepository;
    ServiceRepository serviceRepository;
    BarberWorkingHourRepository barberWorkingHourRepository;
    ReservationRepository reservationRepository;
    BarberMapper barberMapper;
    ServiceMapper serviceMapper;

    public BarberResponseDto addBarber(BarberRequest request) {
        if (barberRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BarberAlreadyExistException("User is already a barber!");
        }

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        if (user.getRole() == Role.ADMIN) {
            throw new AdminCannotBeBarberException("Admin users cannot be assigned as a barber!");
        }

        SalonEntity salon = salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new NoAvailableSalonException("Salon not found!"));

        List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds() != null ? request.getServiceIds() : List.of());

        if (user.getRole() == Role.CUSTOMER) {
            user.setRole(Role.BARBER);
            userRepository.save(user);
        }

        BarberEntity barber = new BarberEntity();
        barber.setUser(user);
        barber.setSalon(salon);
        barber.setDisplayName(request.getDisplayName());
        barber.setServices(services);

        barberRepository.save(barber);

        return barberMapper.toBarberResponseDto(barber);
    }

    public BarberResponseDto updateBarber(Long id, BarberRequest request) {
        BarberEntity barber = barberRepository.findById(id)
                .orElseThrow(() -> new BarberNotFoundException("Barber not found!"));

        SalonEntity salon = salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new NoAvailableSalonException("Salon not found!"));

        List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds() != null ? request.getServiceIds() : List.of());

        barber.setSalon(salon);
        barber.setDisplayName(request.getDisplayName());
        barber.setServices(services);

        barberRepository.save(barber);

        return barberMapper.toBarberResponseDto(barber);
    }

    public BarberResponseDto deleteBarber(Long id) {
        BarberEntity barber = barberRepository.findById(id)
                .orElseThrow(() -> new BarberNotFoundException("Barber not found!"));

        barber.setActive(false);
        barberRepository.save(barber);

        return barberMapper.toBarberResponseDto(barber);
    }

    public Page<BarberResponseDto> getBarbers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return barberRepository.findAllByIsActiveTrue(pageable).map(barberMapper::toBarberResponseDto);
    }

    public BarberResponseDto changeBarberStatus(Long id) {
        BarberEntity barber = barberRepository.findById(id)
                .orElseThrow(() -> new BarberNotFoundException("Barber not found!"));

        barber.setActive(!barber.isActive());
        barberRepository.save(barber);

        return barberMapper.toBarberResponseDto(barber);
    }

    public List<ServiceResponseDto> getBarberServices(Long id) {
        BarberEntity barber = barberRepository.findById(id)
                .orElseThrow(() -> new BarberNotFoundException("Barber not found!"));
        return barber.getServices().stream().map(serviceMapper::toServiceResponseDto).collect(Collectors.toList());
    }

    public List<String> getAvailableSlots(Long barberId, LocalDate date) {
        if (!barberRepository.existsById(barberId)) {
            throw new BarberNotFoundException("Barber not found!");
        }

        BarberWorkingHourEntity workingHour = barberWorkingHourRepository
                .findByBarberIdAndDayOfWeek(barberId, date.getDayOfWeek())
                .orElseThrow(() -> new IllegalArgumentException("The barber's work schedule is not set today!"));

        ZoneId zoneId = ZoneId.of("Asia/Baku");
        Instant startOfDay = date.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<ReservationEntity> reservations = reservationRepository.findAllByBarberIdAndStartAtBetween(barberId, startOfDay, endOfDay);

        List<String> availableSlots = new ArrayList<>();
        LocalTime currentTime = workingHour.getStartTime();
        LocalTime endTime = workingHour.getEndTime();
        LocalTime breakStart = workingHour.getBreakStartTime();
        LocalTime breakEnd = workingHour.getBreakEndTime();

        while (currentTime.plusMinutes(30).isBefore(endTime) || currentTime.plusMinutes(30).equals(endTime)) {
            LocalTime slotEnd = currentTime.plusMinutes(30);

            boolean isBreak = false;
            if (breakStart != null && breakEnd != null) {
                if ((currentTime.isAfter(breakStart) || currentTime.equals(breakStart)) && currentTime.isBefore(breakEnd)) {
                    isBreak = true;
                }
            }

            if (isBreak) {
                currentTime = currentTime.plusMinutes(30);
                continue;
            }

            boolean isReserved = false;
            for (ReservationEntity res : reservations) {
                if (res.getStatus() == ReservationStatus.CANCELLED || res.getStatus() == ReservationStatus.NO_SHOW) continue;

                LocalTime resStart = LocalDateTime.ofInstant(res.getStartAt(), zoneId).toLocalTime();
                LocalTime resEnd = LocalDateTime.ofInstant(res.getEndAt(), zoneId).toLocalTime();

                if (currentTime.isBefore(resEnd) && slotEnd.isAfter(resStart)) {
                    isReserved = true;
                    break;
                }
            }

            if (!isReserved) {
                availableSlots.add(currentTime.toString());
            }

            currentTime = currentTime.plusMinutes(30);
        }

        return availableSlots;
    }
}
