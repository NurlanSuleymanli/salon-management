package com.nurlansuleymanli.salonmanager.exception;

public class ServiceAlreadyExistException extends RuntimeException {
    public ServiceAlreadyExistException(String message) {
        super(message);
    }
}
