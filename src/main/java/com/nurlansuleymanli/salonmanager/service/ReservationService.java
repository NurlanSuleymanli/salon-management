package com.nurlansuleymanli.salonmanager.service;

import com.nurlansuleymanli.salonmanager.exception.BarberNotFoundException;
import com.nurlansuleymanli.salonmanager.exception.ReservationCancellationNotAllowedException;
import com.nurlansuleymanli.salonmanager.exception.ReservationNotFoundException;
import com.nurlansuleymanli.salonmanager.exception.ServiceNotFoundException;
import com.nurlansuleymanli.salonmanager.exception.TimeSlotNotAvailableException;
import com.nurlansuleymanli.salonmanager.mapper.ReservationMapper;
import com.nurlansuleymanli.salonmanager.model.dto.request.ReservationRequest;
import com.nurlansuleymanli.salonmanager.model.dto.request.UpdateReservationStatusRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.ReservationResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.BarberEntity;
import com.nurlansuleymanli.salonmanager.model.entity.ReservationEntity;
import com.nurlansuleymanli.salonmanager.model.entity.ServiceEntity;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.model.enums.ReservationStatus;
import com.nurlansuleymanli.salonmanager.model.enums.Role;
import com.nurlansuleymanli.salonmanager.repository.BarberRepository;
import com.nurlansuleymanli.salonmanager.repository.ReservationRepository;
import com.nurlansuleymanli.salonmanager.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReservationService {

    ReservationRepository reservationRepository;
    BarberRepository barberRepository;
    ServiceRepository serviceRepository;
    ReservationMapper reservationMapper;

    static final ZoneId ZONE = ZoneId.of("Asia/Baku");
    static final List<ReservationStatus> INACTIVE_STATUSES = List.of(ReservationStatus.CANCELLED, ReservationStatus.NO_SHOW);

    public ReservationResponseDto createReservation(UserEntity customer, ReservationRequest request) {
        BarberEntity barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new BarberNotFoundException("Barber not found!"));

        if (!barber.isActive()) {
            throw new IllegalArgumentException("This barber is currently not accepting reservations!");
        }

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException("Service not found!"));

        Instant startAt = LocalDateTime.of(request.getDate(), request.getStartTime())
                .atZone(ZONE).toInstant();

        if (startAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Reservation time cannot be in the past!");
        }

        if (request.getStartTime().getMinute() % 30 != 0) {
            throw new IllegalArgumentException("Reservations can only be made at 30-minute intervals (e.g., 10:00, 10:30)!");
        }

        Instant endAt = startAt.plusSeconds(service.getDurationMin() * 60L);

        List<ReservationEntity> conflicts = reservationRepository.findConflictingReservations(
                barber.getId(), startAt, endAt, INACTIVE_STATUSES
        );

        if (!conflicts.isEmpty()) {
            throw new TimeSlotNotAvailableException("This time slot is already booked. Please choose another time!");
        }

        ReservationEntity reservation = ReservationEntity.builder()
                .customer(customer)
                .barber(barber)
                .service(service)
                .salon(barber.getSalon())
                .startAt(startAt)
                .endAt(endAt)
                .status(ReservationStatus.PENDING)
                .build();

        reservationRepository.save(reservation);

        log.info("New reservation created successfully: ID {} for customer {} with barber {} at {}", 
                reservation.getId(), customer.getEmail(), barber.getId(), startAt);

        return reservationMapper.toReservationResponseDto(reservation);
    }

    public List<ReservationResponseDto> getMyReservations(UserEntity customer) {
        return reservationRepository.findAllByCustomerIdOrderByStartAtDesc(customer.getId())
                .stream()
                .map(reservationMapper::toReservationResponseDto)
                .collect(Collectors.toList());
    }

    public ReservationResponseDto cancelReservation(Long id, UserEntity customer) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found!"));

        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            throw new ReservationCancellationNotAllowedException("You can only cancel your own reservations!");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationCancellationNotAllowedException("This reservation is already cancelled!");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED || reservation.getStatus() == ReservationStatus.NO_SHOW) {
            throw new ReservationCancellationNotAllowedException("Completed or no-show reservations cannot be cancelled!");
        }

        long minutesUntilStart = (reservation.getStartAt().getEpochSecond() - Instant.now().getEpochSecond()) / 60;
        if (minutesUntilStart < 120) {
            throw new ReservationCancellationNotAllowedException("Reservations can only be cancelled at least 2 hours before the appointment!");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(Instant.now());
        reservation.setCancelReason("Cancelled by customer");
        reservationRepository.save(reservation);

        log.info("Reservation ID {} has been successfully cancelled by customer {}", id, customer.getEmail());

        return reservationMapper.toReservationResponseDto(reservation);
    }

    public List<ReservationResponseDto> getBarberSchedule(UserEntity barberUser) {
        BarberEntity barber = barberRepository.findByUserId(barberUser.getId())
                .orElseThrow(() -> new BarberNotFoundException("Barber profile not found for this user!"));

        return reservationRepository.findBarberScheduleFrom(barber.getId(), Instant.now())
                .stream()
                .map(reservationMapper::toReservationResponseDto)
                .collect(Collectors.toList());
    }

    public ReservationResponseDto updateReservationStatus(Long id, UpdateReservationStatusRequest request, UserEntity user) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found!"));

        if (user.getRole() != Role.ADMIN) {
            BarberEntity barber = barberRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BarberNotFoundException("Barber profile not found!"));
            if (!reservation.getBarber().getId().equals(barber.getId())) {
                log.warn("IDOR Attempt: User {} tried to modify reservation {} owned by barber {}", 
                         user.getEmail(), reservation.getId(), reservation.getBarber().getId());
                throw new IllegalArgumentException("Access Denied: You can only update your own reservations!");
            }
        }

        if (request.getStatus() == ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot manually set reservation status back to PENDING!");
        }

        reservation.setStatus(request.getStatus());
        reservationRepository.save(reservation);

        log.info("Reservation ID {} status updated to {}", id, request.getStatus());

        return reservationMapper.toReservationResponseDto(reservation);
    }

    public Page<ReservationResponseDto> getAllReservations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationRepository.findAll(pageable).map(reservationMapper::toReservationResponseDto);
    }
}
