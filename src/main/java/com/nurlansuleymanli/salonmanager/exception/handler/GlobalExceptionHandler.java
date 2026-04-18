package com.nurlansuleymanli.salonmanager.exception.handler;

import com.nurlansuleymanli.salonmanager.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<?> handleEmailExists(EmailAlreadyExistException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(PhoneNumberAlreadyExistException.class)
    public ResponseEntity<?> handlePhoneExists(PhoneNumberAlreadyExistException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<?> handleWrongPassword(WrongPasswordException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password. Please try again."));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledAccount(org.springframework.security.authentication.DisabledException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Your account is disabled. Please contact the administrator."));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredToken(ExpiredJwtException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Your session has expired. Please log in again."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(Exception e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid URL parameter. Please use a valid numeric ID."));
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<?> handleServiceNotFound(ServiceNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Service not found."));
    }

    @ExceptionHandler(ServiceAlreadyExistException.class)
    public ResponseEntity<?> handleServiceAlreadyExists(ServiceAlreadyExistException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "A service with this name already exists."));
    }

    @ExceptionHandler(BarberNotFoundException.class)
    public ResponseEntity<?> handleBarberNotFound(BarberNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Barber not found."));
    }

    @ExceptionHandler(BarberAlreadyExistException.class)
    public ResponseEntity<?> handleBarberAlreadyExist(BarberAlreadyExistException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "This user is already registered as a barber."));
    }

    @ExceptionHandler(AdminCannotBeBarberException.class)
    public ResponseEntity<?> handleAdminCannotBeBarber(AdminCannotBeBarberException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<?> handleReservationNotFound(ReservationNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(TimeSlotNotAvailableException.class)
    public ResponseEntity<?> handleTimeSlotNotAvailable(TimeSlotNotAvailableException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(ReservationCancellationNotAllowedException.class)
    public ResponseEntity<?> handleCancellationNotAllowed(ReservationCancellationNotAllowedException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<?> handleSamePassword(SamePasswordException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(NoAvailableSalonException.class)
    public ResponseEntity<?> handleNoAvailableSalon(NoAvailableSalonException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {

        log.warn("Validation Error: {}", e.getMessage());

        // Collect all field errors into a single readable message
        String details = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> "'" + fe.getField() + "': " + fe.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining("; "));

        String message = (details == null || details.isBlank())
                ? "The submitted data is invalid. Please check your input and try again."
                : "Validation failed — " + details;

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralExceptions(Exception e){

        log.error("Internal Server Error: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An unexpected error occurred. Please try again later."));
    }

}
