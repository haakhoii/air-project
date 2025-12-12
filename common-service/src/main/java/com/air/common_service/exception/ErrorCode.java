package com.air.common_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1001, "You do not have permission", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    USER_EXISTS(1003, "User ready exists", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(1004, "Incorrect password", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005, "User not found", HttpStatus.NOT_FOUND),
    ALREADY_LOGGED_OUT(1006, "Already logged out", HttpStatus.BAD_REQUEST),
    FLIGHT_NOT_FOUND(1007, "Flight not found", HttpStatus.NOT_FOUND),
    SEAT_ALREADY_HELD(1008, "Seat already held", HttpStatus.BAD_REQUEST),
    SEAT_ALREADY_BOOKED(1009, "Seat already booked", HttpStatus.BAD_REQUEST),
    SEAT_NOT_HELD(1010, "Seat not held", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_PENDING(1011, "Booking already pending", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(1012, "Booking not found", HttpStatus.NOT_FOUND),
    BOOKING_INVALID_STATUS(1013, "Booking invalid status", HttpStatus.BAD_REQUEST),
    BOOKING_EXPIRED(1014, "Booking expired", HttpStatus.BAD_REQUEST),
    SEAT_NOT_FOUND(1015, "Seat not found", HttpStatus.NOT_FOUND),
    BOOKING_NOT_PENDING(1016, "Booking not found", HttpStatus.NOT_FOUND),
    PAYMENT_FAILED(1017, "Payment failed", HttpStatus.BAD_REQUEST),
    SEAT_HOLD_FAILED(1018, "Seat hold failed",  HttpStatus.NOT_FOUND),
    BOOKING_FAILED(1019, "Booking failed", HttpStatus.BAD_REQUEST),
    SYSTEM_BUSY(1020, "System busy", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_ERROR(1021, "System error", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_NOT_FOUND(1022, "Payment not found", HttpStatus.NOT_FOUND),
    SEAT_HOLD_EXPIRED(1023, "Seat hold expired", HttpStatus.BAD_REQUEST),
    SEAT_HOLD_INVALID(1024, "Seat hold invalid", HttpStatus.BAD_REQUEST)
    ;

    private final int code;

    private final String message;

    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
