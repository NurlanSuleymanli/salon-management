package com.nurlansuleymanli.salonmanager.controller;

import com.nurlansuleymanli.salonmanager.model.dto.request.ReservationRequest;
import com.nurlansuleymanli.salonmanager.model.dto.request.UpdateReservationStatusRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.ReservationResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.service.ReservationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationController {

    ReservationService reservationService;

    @PostMapping("/create")
    public ResponseEntity<ReservationResponseDto> createReservation(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody @Valid ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(user, request));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations(
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(reservationService.getMyReservations(user));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDto> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, user));
    }

    @GetMapping("/barber-schedule")
    public ResponseEntity<List<ReservationResponseDto>> getBarberSchedule(
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(reservationService.getBarberSchedule(user));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationResponseDto> updateReservationStatus(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody @Valid UpdateReservationStatusRequest request) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, request, user));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ReservationResponseDto>> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reservationService.getAllReservations(page, size));
    }
}
