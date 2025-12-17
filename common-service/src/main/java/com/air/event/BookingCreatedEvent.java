package com.air.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingCreatedEvent {
    private String bookingId;
    private String userId;
    private String createdBy;
    private String flightId;
    private List<String> seatIds;
    private Double totalPrice;
    private String bookingStatus;
}
