package com.nurlansuleymanli.salonmanager.controller;


import com.nurlansuleymanli.salonmanager.model.dto.request.ServiceRequest;
import com.nurlansuleymanli.salonmanager.model.dto.response.ServiceResponseDto;
import com.nurlansuleymanli.salonmanager.service.ServicesService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/add")
    public ResponseEntity<?> createService(@RequestBody @Valid ServiceRequest request){

        ServiceResponseDto response = servicesService.createService(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateService(@RequestBody @Valid ServiceRequest request){

        ServiceResponseDto response = servicesService.updateService(request);

        return ResponseEntity.ok(response);

    }




}
