package com.air.common_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightRequest {
    String flightCode;

    String departure;

    String destination;

    String departureTime;

    String destinationTime;

    Double priceEconomy;

    Integer seatsEconomy;

    Double priceBusiness;

    Integer seatsBusiness;
}
