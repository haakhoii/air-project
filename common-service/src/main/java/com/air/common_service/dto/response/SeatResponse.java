package com.air.common_service.dto.response;

import com.air.common_service.constants.SeatClass;
import com.air.common_service.constants.SeatStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatResponse {
    String id;

    String flightId;

    SeatClass seatClass;

    String seatNumber;

    SeatStatus seatStatus;

    Double price;
}
