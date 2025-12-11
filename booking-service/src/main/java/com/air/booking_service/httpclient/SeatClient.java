package com.air.booking_service.httpclient;

import com.air.booking_service.config.FeignClientConfig;
import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.request.SeatRequest;
import com.air.common_service.dto.response.SeatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "seat-service",
        url = "${app.services.seats}",
        configuration = FeignClientConfig.class
)
public interface SeatClient {
    @PostMapping("/internal/create")
    ApiResponse<List<SeatResponse>> create(@RequestBody SeatRequest request);

    @GetMapping("/internal/flight/{flightId}")
    ApiResponse<List<SeatResponse>> getSeatsByFlight(@PathVariable("flightId") String flightId);

    @PostMapping("/internal/hold")
    ApiResponse<List<SeatResponse>> hold(@RequestBody HoldSeatRequest request);

    @PostMapping("/internal/cancel")
    ApiResponse<List<SeatResponse>> cancel(@RequestBody HoldSeatRequest request);

    @PostMapping("/internal/book")
    ApiResponse<List<SeatResponse>> bookSeats(@RequestBody HoldSeatRequest request);

    @PostMapping("/internal/get-seats")
    ApiResponse<List<SeatResponse>> getSeats(@RequestBody HoldSeatRequest request);
}
