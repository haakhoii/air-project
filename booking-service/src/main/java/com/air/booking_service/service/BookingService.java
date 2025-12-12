package com.air.booking_service.service;

import com.air.booking_service.entity.Booking;
import com.air.booking_service.httpclient.FlightClient;
import com.air.booking_service.httpclient.SeatClient;
import com.air.booking_service.mapper.BookingMapper;
import com.air.booking_service.repository.BookingRepository;
import com.air.common_service.constants.BookingStatus;
import com.air.common_service.constants.SeatClass;
import com.air.common_service.dto.request.BookingCreateRequest;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.CancelBookingResponse;
import com.air.common_service.dto.response.FlightResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {
    BookingRepository bookingRepository;
    FlightClient flightClient;
    SeatClient seatClient;
    RedisTemplate<String, Object> redisTemplate;

    String BOOKING_KEY = "booking:pending:";
    long BOOKING_TTL_SECONDS = 60;

    @Transactional
    public BookingResponse book(BookingCreateRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        HoldSeatRequest holdRequest = new HoldSeatRequest(request.getSeatIds());
        List<SeatResponse> seats = seatClient.hold(holdRequest).getResult();
        String flightId = seats.get(0).getFlightId();
        FlightResponse flight = flightClient.getFlightById(flightId).getResult();

        double totalPrice = seats.stream()
                .mapToDouble(s -> s.getSeatClass() == SeatClass.ECONOMY
                        ? flight.getPriceEconomy()
                        : flight.getPriceBusiness())
                .sum();

        Booking booking = Booking.builder()
                .userId(userId)
                .flightId(flightId)
                .seatIds(request.getSeatIds())
                .totalPrice(totalPrice)
                .bookingStatus(BookingStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        booking = bookingRepository.save(booking);

        String redisKey = BOOKING_KEY + booking.getId();

        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                redisKey,
                userId,
                BOOKING_TTL_SECONDS,
                TimeUnit.SECONDS
        );

        if (Boolean.FALSE.equals(success)) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_PENDING);
        }

        return BookingMapper.toBookingResponse(booking, seats);
    }

    @Transactional
    public void releaseBooking(String bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElse(null);

        if (booking == null) return;

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            return;
        }

        booking.setBookingStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);

        if (booking.getSeatIds() != null && !booking.getSeatIds().isEmpty()) {
            HoldSeatRequest request = new HoldSeatRequest(booking.getSeatIds());
            seatClient.cancel(request);
        }

        String redisKey = BOOKING_KEY + bookingId;
        redisTemplate.delete(redisKey);

        bookingRepository.delete(booking);
    }

    public CancelBookingResponse cancelSeat(HoldSeatRequest request) {
        String holdBy = SecurityContextHolder.getContext().getAuthentication().getName();

        List<SeatResponse> seats = seatClient.cancel(request).getResult();

        Booking booking = bookingRepository
                .findByUserIdAndFlightIdAndBookingStatus(
                        holdBy,
                        seats.get(0).getFlightId(),
                        BookingStatus.PENDING
                )
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        bookingRepository.delete(booking);

        String redisKey = "booking:pending:" + booking.getId();
        redisTemplate.delete(redisKey);

        CancelBookingResponse cancelBookingResponse = BookingMapper.toCancelBookingResponse(booking, seats);
        cancelBookingResponse.setBookingStatus(BookingStatus.CANCEL);

        return cancelBookingResponse;
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        HoldSeatRequest getSeatsRequest = new HoldSeatRequest(booking.getSeatIds());
        List<SeatResponse> seats = seatClient.getSeats(getSeatsRequest).getResult();


        return BookingMapper.toBookingResponse(booking, seats);
    }

    @Transactional
    public void markPaid(String bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_INVALID_STATUS);
        }

        String redisKey = BOOKING_KEY + bookingId;
        Boolean existed = redisTemplate.hasKey(redisKey);

        if (Boolean.FALSE.equals(existed)) {
            throw new AppException(ErrorCode.BOOKING_EXPIRED);
        }

        booking.setBookingStatus(BookingStatus.PAID);
        bookingRepository.save(booking);

        redisTemplate.delete(redisKey);
    }

    @Transactional
    public void markCancelled(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        booking.setBookingStatus(BookingStatus.CANCEL);
        bookingRepository.save(booking);
    }

}
