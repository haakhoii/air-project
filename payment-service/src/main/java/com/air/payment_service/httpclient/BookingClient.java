package com.air.payment_service.httpclient;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.response.BookingResponse;
import com.air.payment_service.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "booking-service",
        url = "${app.services.bookings}",
        configuration = FeignClientConfig.class
)
public interface BookingClient {

    @GetMapping("/get/{bookingId}")
    ApiResponse<BookingResponse> getBookingById(@PathVariable("bookingId") String bookingId);

    @PostMapping("/mark-paid/{bookingId}")
    ApiResponse<String> markPaid(@PathVariable("bookingId") String bookingId);
}

