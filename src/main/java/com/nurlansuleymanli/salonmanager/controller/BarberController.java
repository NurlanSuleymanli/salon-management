package com.nurlansuleymanli.salonmanager.controller;

import com.nurlansuleymanli.salonmanager.model.dto.request.BarberRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.BarberResponseDto;
import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.service.BarberService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/barbers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BarberController {

    BarberService barberService;

    @PostMapping("/add")
    public ResponseEntity<BarberResponseDto> addBarber(@RequestBody @Valid BarberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(barberService.addBarber(request));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<BarberResponseDto> updateBarber(@PathVariable Long id, @RequestBody @Valid BarberRequest request) {
        return ResponseEntity.ok(barberService.updateBarber(id, request));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<BarberResponseDto> deleteBarber(@PathVariable Long id) {
        return ResponseEntity.ok(barberService.deleteBarber(id));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<BarberResponseDto>> getBarbers(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(barberService.getBarbers(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberResponseDto> getBarberById(@PathVariable Long id) {
        return ResponseEntity.ok(barberService.getBarberById(id));
    }

    @GetMapping("/salon/{salonId}")
    public ResponseEntity<List<BarberResponseDto>> getBarbersBySalonId(@PathVariable Long salonId) {
        return ResponseEntity.ok(barberService.getBarbersBySalonId(salonId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BarberResponseDto> changeBarberStatus(@PathVariable Long id) {
        return ResponseEntity.ok(barberService.changeBarberStatus(id));
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceResponseDto>> getBarberServices(@PathVariable Long id) {
        return ResponseEntity.ok(barberService.getBarberServices(id));
    }

    @GetMapping("/{id}/available-slots")
    public ResponseEntity<List<String>> getAvailableSlots(
            @PathVariable Long id, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "30") Integer duration) {
        return ResponseEntity.ok(barberService.getAvailableSlots(id, date, duration));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<BarberResponseDto>> getBarbersByServiceId(@RequestParam Long serviceId) {
        return ResponseEntity.ok(barberService.getBarbersByServiceId(serviceId));
    }

    @PutMapping("/my-services")
    public ResponseEntity<BarberResponseDto> updateMyServices(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody List<Long> serviceIds) {
        return ResponseEntity.ok(barberService.updateMyServices(user, serviceIds));
    }

    @GetMapping("/me")
    public ResponseEntity<BarberResponseDto> getMyProfile(
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(barberService.getMyProfile(user));
    }
}
