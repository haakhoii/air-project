package com.air.orchestrator_service.httpclient;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.BookingCreateRequest;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.CancelBookingResponse;
import com.air.orchestrator_service.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "booking-service", url = "${app.services.bookings}", configuration = FeignClientConfig.class)
public interface BookingClient {
    @PostMapping("/book")
    ApiResponse<BookingResponse> booking(@RequestBody BookingCreateRequest request);

    @PostMapping("/cancel-seat")
    ApiResponse<CancelBookingResponse> cancelSeat(@RequestBody HoldSeatRequest request);

    @PostMapping("/mark-paid/{bookingId}")
    ApiResponse<String> markPaid(@PathVariable("bookingId") String bookingId);

    @GetMapping("/get/{bookingId}")
    ApiResponse<BookingResponse> getBooking(@PathVariable("bookingId") String bookingId);

    @PostMapping("/mark-cancelled/{bookingId}")
    ApiResponse<String> markCancelled(@PathVariable("bookingId") String bookingId);

    @PostMapping("/admin/book")
    ApiResponse<BookingResponse> adminBookForGuest(@RequestBody BookingCreateRequest request);

}