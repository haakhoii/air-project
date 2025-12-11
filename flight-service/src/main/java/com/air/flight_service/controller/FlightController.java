package com.air.flight_service.controller;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.FlightRequest;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.response.FlightSeatResponse;
import com.air.common_service.dto.response.FlightResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.flight_service.service.FlightService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightController {
    FlightService flightService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<FlightResponse> createFlight(@RequestBody FlightRequest request) {
        return ApiResponse.<FlightResponse>builder()
                .result(flightService.create(request))
                .build();
    }

    @GetMapping("/get-all")
    ApiResponse<List<FlightResponse>> getAllFlights() {
        return ApiResponse.<List<FlightResponse>>builder()
                .result(flightService.getAll())
                .build();
    }

    @GetMapping("/get-by-id/{flightId}")
    ApiResponse<FlightResponse> getFlightById(@PathVariable("flightId") String flightId) {
        return ApiResponse.<FlightResponse>builder()
                .result(flightService.getById(flightId))
                .build();
    }

    @GetMapping("/get-all-seat/{flightId}")
    ApiResponse<List<SeatResponse>> getSeatByFlight(@PathVariable("flightId") String flightId) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(flightService.getSeatsByFlight(flightId))
                .build();
    }

    @PostMapping("/hold-seat")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    ApiResponse<FlightSeatResponse> holdSeat(@RequestBody HoldSeatRequest request) {
        return ApiResponse.<FlightSeatResponse>builder()
                .result(flightService.holdSeat(request))
                .build();
    }

    @PostMapping("/cancel-seat")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    ApiResponse<FlightSeatResponse> cancelSeat(@RequestBody HoldSeatRequest request) {
        return ApiResponse.<FlightSeatResponse>builder()
                .result(flightService.cancelSeat(request))
                .build();
    }
}
