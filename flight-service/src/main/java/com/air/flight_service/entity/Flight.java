package com.air.flight_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "tbl_flight")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Flight implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String createdBy;

    String flightCode;

    String departure;

    String destination;

    Instant departureTime;

    Instant destinationTime;

    Double priceEconomy;

    Integer seatsEconomy;

    Double priceBusiness;

    Integer seatsBusiness;

    Integer totalSeats;
}
