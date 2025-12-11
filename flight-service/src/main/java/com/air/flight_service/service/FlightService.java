package com.air.flight_service.service;

import com.air.common_service.config.DateTimeConfig;
import com.air.common_service.constants.SeatClass;
import com.air.common_service.dto.request.FlightRequest;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.request.SeatRequest;
import com.air.common_service.dto.response.FlightSeatResponse;
import com.air.common_service.dto.response.FlightResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
import com.air.flight_service.entity.Flight;
import com.air.flight_service.httpclient.SeatClient;
import com.air.flight_service.mapper.FlightMapper;
import com.air.flight_service.repository.FlightRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightService {
    FlightRepository flightRepository;
    DateTimeConfig dateTimeConfig;
    SeatClient seatClient;

    public FlightResponse create(FlightRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String createdBy = authentication.getName();

        Flight flight = FlightMapper.toFlight(request);
        flight.setCreatedBy(createdBy);

        flight.setDepartureTime(dateTimeConfig.parse(request.getDepartureTime()));
        flight.setDestinationTime(dateTimeConfig.parse(request.getDestinationTime()));

        Flight savedFlight = flightRepository.save(flight);

        SeatRequest seatRequest = SeatRequest.builder()
                .flightId(savedFlight.getId())
                .build();
        seatClient.create(seatRequest);

        FlightResponse response = FlightMapper.toFlightResponse(savedFlight);
        response.setDepartureTime(dateTimeConfig.format(savedFlight.getDepartureTime()));
        response.setDestinationTime(dateTimeConfig.format(savedFlight.getDestinationTime()));

        return response;
    }

    public List<FlightResponse> getAll() {
        List<Flight> flights = flightRepository.findAll();
        return flights.stream().map(f -> {
            FlightResponse r = FlightMapper.toFlightResponse(f);
            r.setDepartureTime(dateTimeConfig.format(f.getDepartureTime()));
            r.setDestinationTime(dateTimeConfig.format(f.getDestinationTime()));
            return r;
        }).collect(Collectors.toList());
    }

    public FlightResponse getById(String flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));
        FlightResponse response = FlightMapper.toFlightResponse(flight);
        response.setDepartureTime(dateTimeConfig.format(flight.getDepartureTime()));
        response.setDestinationTime(dateTimeConfig.format(flight.getDestinationTime()));
        return response;
    }

    public List<SeatResponse> getSeatsByFlight(String flightId) {
        return seatClient.getSeatsByFlight(flightId).getResult();
    }

    public FlightSeatResponse holdSeat(HoldSeatRequest request) {
        String holdBy = SecurityContextHolder.getContext().getAuthentication().getName();
        List<SeatResponse> seats = seatClient.hold(request).getResult();
        String flightId = seats.get(0).getFlightId();
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        double totalPrice = seats.stream()
                .mapToDouble(s -> {
                    if (s.getSeatClass() == SeatClass.ECONOMY) {
                        return flight.getPriceEconomy();
                    } else {
                        return flight.getPriceBusiness();
                    }
                })
                .sum();

        FlightSeatResponse flightSeatResponse = FlightMapper.toFlightSeatResponse(holdBy, seats);
        flightSeatResponse.setTotalPrice(totalPrice);

        return flightSeatResponse;
    }

    public FlightSeatResponse cancelSeat(HoldSeatRequest request) {
        String holdBy = SecurityContextHolder.getContext().getAuthentication().getName();
        List<SeatResponse> seats = seatClient.cancel(request).getResult();

        return FlightMapper.toFlightSeatResponse(holdBy, seats);
    }
}


