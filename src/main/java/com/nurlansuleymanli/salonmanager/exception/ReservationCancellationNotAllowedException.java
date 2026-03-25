package com.nurlansuleymanli.salonmanager.exception;

public class ReservationCancellationNotAllowedException extends RuntimeException {
    public ReservationCancellationNotAllowedException(String message) {
        super(message);
    }
}
