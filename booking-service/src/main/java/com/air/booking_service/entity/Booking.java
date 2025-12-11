package com.air.booking_service.entity;

import com.air.common_service.constants.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tbl_booking")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String userId;

    String flightId;

    List<String> seatIds;

    Double totalPrice;

    @Enumerated(EnumType.STRING)
    BookingStatus bookingStatus;

    Instant createdAt;
}
