package com.air.booking_service.controller;

import com.air.booking_service.service.BookingService;
import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.BookingCreateRequest;
import com.air.common_service.dto.request.BookingRequest;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.BookingSeatResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    BookingService bookingService;

    @PostMapping("/book")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    ApiResponse<BookingResponse> booking(@RequestBody BookingCreateRequest request) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.book(request))
                .build();
    }

    @PostMapping("/cancel-seat")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    ApiResponse<BookingSeatResponse> cancelSeat(@RequestBody HoldSeatRequest request) {
        return ApiResponse.<BookingSeatResponse>builder()
                .result(bookingService.cancelSeat(request))
                .build();
    }

    @GetMapping("/get/{bookingId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    ApiResponse<BookingResponse> getBooking(@PathVariable("bookingId") String bookingId) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.getBooking(bookingId))
                .build();
    }

    @PostMapping("/mark-paid/{bookingId}")
    public ApiResponse<String> markPaid(@PathVariable("bookingId") String bookingId) {
        bookingService.markPaid(bookingId);
        return ApiResponse.<String>builder()
                .result("UPDATED")
                .build();
    }
}
