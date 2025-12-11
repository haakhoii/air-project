package com.air.common_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightSeatResponse {
    List<SeatResponse> seats;

    String holdBy;

    Double totalPrice;
}
