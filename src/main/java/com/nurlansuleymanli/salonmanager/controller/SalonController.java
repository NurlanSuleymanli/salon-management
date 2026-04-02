package com.nurlansuleymanli.salonmanager.controller;

import com.nurlansuleymanli.salonmanager.model.dto.request.SalonRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.SalonResponse;
import com.nurlansuleymanli.salonmanager.service.SalonService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/salons")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SalonController {

    SalonService salonService;

    @PostMapping("/create")
    public ResponseEntity<SalonResponse> createSalon(@RequestBody @Valid SalonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salonService.createSalon(request));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<SalonResponse> updateSalon(@PathVariable Long id, @RequestBody @Valid SalonRequest request) {
        return ResponseEntity.ok(salonService.updateSalon(id, request));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<SalonResponse> deleteSalon(@PathVariable Long id) {
        return ResponseEntity.ok(salonService.deleteSalon(id));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<SalonResponse>> getSalons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(salonService.getSalons(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalonResponse> getSalonById(@PathVariable Long id) {
        return ResponseEntity.ok(salonService.getSalonById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SalonResponse>> searchSalons(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(salonService.searchSalons(name, page, size));
    }
}
