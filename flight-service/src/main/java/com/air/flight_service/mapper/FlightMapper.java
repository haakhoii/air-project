package com.air.flight_service.mapper;

import com.air.common_service.dto.request.FlightRequest;
import com.air.common_service.dto.response.FlightSeatResponse;
import com.air.common_service.dto.response.FlightResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.flight_service.entity.Flight;

import java.util.List;

public class FlightMapper {

    public static Flight toFlight(FlightRequest request) {
        return Flight.builder()
                .flightCode(request.getFlightCode())
                .departure(request.getDeparture())
                .destination(request.getDestination())
                .priceEconomy(request.getPriceEconomy())
                .seatsEconomy(request.getSeatsEconomy())
                .priceBusiness(request.getPriceBusiness())
                .seatsBusiness(request.getSeatsBusiness())
                .totalSeats(request.getSeatsEconomy() + request.getSeatsBusiness())
                .build();
    }

    public static FlightResponse toFlightResponse(Flight flight) {
        return FlightResponse.builder()
                .id(flight.getId())
                .createdBy(flight.getCreatedBy())
                .flightCode(flight.getFlightCode())
                .departure(flight.getDeparture())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime().toString())
                .destinationTime(flight.getDestinationTime().toString())
                .priceEconomy(flight.getPriceEconomy())
                .seatsEconomy(flight.getSeatsEconomy())
                .priceBusiness(flight.getPriceBusiness())
                .seatsBusiness(flight.getSeatsBusiness())
                .totalSeats(flight.getTotalSeats())
                .build();
    }

    public static FlightSeatResponse toFlightSeatResponse(String holdBy, List<SeatResponse> seats) {
        return FlightSeatResponse.builder()
                .seats(seats)
                .holdBy(holdBy)
                .totalPrice(0.0)
                .build();
    }
}
