package com.air.booking_service.mapper;

import com.air.booking_service.entity.Booking;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.CancelBookingResponse;
import com.air.common_service.dto.response.SeatResponse;

import java.util.List;

public class BookingMapper {
    public static BookingResponse toBookingResponse(Booking booking, List<SeatResponse> seats) {
        return BookingResponse.builder()
                .id(booking.getId())
                .flightId(booking.getFlightId())
                .userId(booking.getUserId())
                .createdBy(booking.getCreatedBy())
                .seats(seats)
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .build();
    }

    public static CancelBookingResponse toCancelBookingResponse(Booking booking, List<SeatResponse> seats) {
        return CancelBookingResponse.builder()
                .id(booking.getId())
                .flightId(booking.getFlightId())
                .userId(booking.getUserId())
                .seats(seats)
                .totalPrice(0.0)
                .build();
    }
}
