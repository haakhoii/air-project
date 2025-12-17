package com.air.orchestrator_service.controller;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.AdminCreateBookingRequest;
import com.air.common_service.dto.request.BookAndPayRequest;
import com.air.common_service.dto.request.BookingCreateRequest;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.PaymentResponse;
import com.air.orchestrator_service.service.OrchestratorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrchestratorController {

    OrchestratorService orchestratorService;

    @PostMapping("/create-booking")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<BookingResponse> createBookingAndHold(
            @RequestBody BookingCreateRequest request
    ) {
        BookingResponse result = orchestratorService.createBookingAndHold(request);
        return ApiResponse.<BookingResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/payment")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PaymentResponse> bookAndPay(
            @RequestBody BookAndPayRequest request
    ) {
        PaymentResponse result = orchestratorService.pay(request.getBookingId(), request.getPaymentMethod());
        return ApiResponse.<PaymentResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/cancel/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PaymentResponse> cancelBooking(@PathVariable("bookingId") String bookingId) {
        return ApiResponse.<PaymentResponse>builder()
                .result(orchestratorService.cancelBooking(bookingId))
                .build();
    }

    @PostMapping("/admin/booking")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BookingResponse> adminCreateBooking(
            @RequestBody AdminCreateBookingRequest request
    ) {
        return ApiResponse.<BookingResponse>builder()
                .result(orchestratorService.adminCreateBookingAndHold(request))
                .build();
    }

    @PostMapping("/admin/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaymentResponse> adminPayCash(
            @RequestParam("bookingId") String bookingId
    ) {
        return ApiResponse.<PaymentResponse>builder()
                .result(orchestratorService.adminPayForGuest(bookingId))
                .build();
    }

}

