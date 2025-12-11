package com.air.common_service.dto.request;

import com.air.common_service.constants.PaymentMethod;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateRequest {
    private String flightId;
    private List<String> seatIds;
}
