package com.air.seat_service.controller;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.request.SeatRequest;
import com.air.common_service.dto.response.SeatResponse;
import com.air.seat_service.service.SeatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatController {
    SeatService seatService;

    @PostMapping("/internal/create")
    ApiResponse<List<SeatResponse>> createSeat(@RequestBody SeatRequest request) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(seatService.create(request))
                .build();
    }

    @GetMapping("/internal/flight/{flightId}")
    ApiResponse<List<SeatResponse>> getSeatsByFlight(@PathVariable("flightId") String flightId) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(seatService.getSeatsByFlight(flightId))
                .build();
    }

    @PostMapping("/internal/hold")
    ApiResponse<List<SeatResponse>> hold(@RequestBody HoldSeatRequest request) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(seatService.holdSeat(request))
                .build();
    }

    @PostMapping("/internal/cancel")
    ApiResponse<List<SeatResponse>> cancel(@RequestBody HoldSeatRequest request) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(seatService.cancelHold(request))
                .build();
    }

    @PostMapping("/internal/book")
    ApiResponse<List<SeatResponse>> bookSeats(@RequestBody HoldSeatRequest request) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(seatService.bookSeats(request))
                .build();
    }

    @PostMapping("/internal/get-seats")
    ApiResponse<List<SeatResponse>> getSeats(@RequestBody HoldSeatRequest request) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(seatService.getSeats(request))
                .build();
    }

}
