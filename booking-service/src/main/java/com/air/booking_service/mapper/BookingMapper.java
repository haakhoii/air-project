package com.air.booking_service.mapper;

import com.air.booking_service.entity.Booking;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.BookingSeatResponse;
import com.air.common_service.dto.response.SeatResponse;

import java.util.List;

public class BookingMapper {
    public static BookingResponse toBookingResponse(Booking booking, List<SeatResponse> seats) {
        return BookingResponse.builder()
                .id(booking.getId())
                .flightId(booking.getFlightId())
                .userId(booking.getUserId())
                .seats(seats)
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .build();
    }

    public static BookingSeatResponse toBookingSeatResponse(String holdBy, List<SeatResponse> seats) {
        return BookingSeatResponse.builder()
                .holdBy(holdBy)
                .seats(seats)
                .totalPrice(0.0)
                .build();
    }
}
