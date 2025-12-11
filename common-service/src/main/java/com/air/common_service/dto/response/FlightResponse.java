package com.air.common_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightResponse {
    String id;

    String createdBy;

    String flightCode;

    String departure;

    String destination;

    String departureTime;

    String destinationTime;

    Double priceEconomy;

    Integer seatsEconomy;

    Double priceBusiness;

    Integer seatsBusiness;

    Integer totalSeats;
}
