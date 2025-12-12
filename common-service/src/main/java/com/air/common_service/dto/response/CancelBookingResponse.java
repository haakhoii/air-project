package com.air.common_service.dto.response;

import com.air.common_service.constants.BookingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelBookingResponse {
    String id;

    String flightId;

    String userId;

    List<SeatResponse> seats;

    Double totalPrice;

    BookingStatus bookingStatus;
}
