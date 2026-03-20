package com.nurlansuleymanli.salonmanager.exception.handler;

import com.nurlansuleymanli.salonmanager.exception.EmailAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.PhoneNumberAlreadyExistException;
import com.nurlansuleymanli.salonmanager.exception.UserNotFoundException;
import com.nurlansuleymanli.salonmanager.exception.WrongPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

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

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<?> handleDisabledAccount(org.springframework.security.authentication.DisabledException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Your account has been blocked by the admin or has not been activated yet!"));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralExceptions(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error_description", e.getMessage(), "message", "An internal system error occurred!"));
    }

}
