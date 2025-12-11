package com.air.booking_service.repository;

import com.air.booking_service.entity.Booking;
import com.air.common_service.constants.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String> {
    Optional<Booking> findByUserIdAndFlightIdAndBookingStatus(String holdBy, String flightId, BookingStatus bookingStatus);
}
