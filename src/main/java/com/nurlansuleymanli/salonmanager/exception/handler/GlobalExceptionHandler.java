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
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Email or password is incorrect!"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledAccount(org.springframework.security.authentication.DisabledException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Your account has been blocked by the admin or has not been activated yet!"));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredToken(ExpiredJwtException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message" , "Access Token is expired!"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(Exception e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Please use only numbers in URL parameters!"));
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<?> handleServiceAlready(ServiceNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Service not found!"));
    }

    @ExceptionHandler(ServiceAlreadyExistException.class)
    public ResponseEntity<?> handleServiceAlready(ServiceAlreadyExistException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Service already exist!"));
    }

    @ExceptionHandler(BarberNotFoundException.class)
    public ResponseEntity<?> handleBarberNotFound(BarberNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Barber not found!"));
    }

    @ExceptionHandler(BarberAlreadyExistException.class)
    public ResponseEntity<?> handleBarberAlreadyExist(BarberAlreadyExistException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Barber already exist!"));
    }

    @ExceptionHandler(AdminCannotBeBarberException.class)
    public ResponseEntity<?> handleAdminCannotBeBarber(AdminCannotBeBarberException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {

        log.info(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Validation error: Data entered in incorrect format!"));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralExceptions(Exception e){

        log.info(e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of( "message", "An internal system error has occurred! Please try again later."));
    }

}
