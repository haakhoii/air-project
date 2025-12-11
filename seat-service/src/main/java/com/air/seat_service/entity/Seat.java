package com.air.seat_service.entity;

import com.air.common_service.constants.SeatClass;
import com.air.common_service.constants.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Entity
@Table(name = "tbl_seat")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String flightId;

    @Enumerated(EnumType.STRING)
    SeatClass seatClass;

    String seatNumber;

    @Enumerated(EnumType.STRING)
    SeatStatus seatStatus;

    String holdBy;
}
