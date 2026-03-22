package com.nurlansuleymanli.salonmanager.controller;


import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.service.ServicesService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ServiceController {

    ServicesService servicesService;

    @GetMapping("/list")
    public ResponseEntity<?> getServices( @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {

        Page<ServiceResponseDto> responsePage = servicesService.getServices(page, size);

        return ResponseEntity.ok(responsePage);

    }
}
