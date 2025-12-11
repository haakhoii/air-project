package com.air.seat_service.mapper;

import com.air.common_service.constants.SeatClass;
import com.air.common_service.dto.response.FlightResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.seat_service.entity.Seat;

import java.util.List;
import java.util.stream.Collectors;

public class SeatMapper {
    public static SeatResponse toSeatResponse(Seat seat, Double price) {
        return SeatResponse.builder()
                .id(seat.getId())
                .flightId(seat.getFlightId())
                .seatClass(seat.getSeatClass())
                .seatNumber(seat.getSeatNumber())
                .seatStatus(seat.getSeatStatus())
                .price(price)
                .build();
    }

    public static List<SeatResponse> toSeatResponseList(List<Seat> seats, FlightResponse flight) {
        return seats.stream()
                .map(seat -> {
                    Double price = seat.getSeatClass() == SeatClass.ECONOMY
                            ? flight.getPriceEconomy()
                            : flight.getPriceBusiness();
                    return toSeatResponse(seat, price);
                })
                .collect(Collectors.toList());
    }
}
