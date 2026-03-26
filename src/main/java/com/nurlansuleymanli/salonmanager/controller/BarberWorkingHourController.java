package com.nurlansuleymanli.salonmanager.controller;

import com.nurlansuleymanli.salonmanager.model.dto.request.BarberWorkingHourRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.BarberWorkingHourResponse;
import com.nurlansuleymanli.salonmanager.model.entity.UserEntity;
import com.nurlansuleymanli.salonmanager.service.BarberWorkingHourService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/working-hours")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BarberWorkingHourController {

    BarberWorkingHourService service;

    @PostMapping("/set")
    public ResponseEntity<BarberWorkingHourResponse> setWorkingHours(
            @AuthenticationPrincipal UserEntity user, 
            @RequestBody @Valid BarberWorkingHourRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.setWorkingHour(user, request));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<BarberWorkingHourResponse> updateWorkingHours(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long id,
            @RequestBody @Valid BarberWorkingHourRequest request) {
        return ResponseEntity.ok(service.updateWorkingHour(user, id, request));
    }

    @GetMapping("/my-schedule")
    public ResponseEntity<List<BarberWorkingHourResponse>> getMySchedule(
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(service.getMyWorkingHours(user));
    }
}
