package com.air.orchestrator_service.service;

import com.air.common_service.constants.BookingStatus;
import com.air.common_service.constants.PaymentMethod;
import com.air.common_service.dto.request.BookingCreateRequest;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.request.PaymentRequest;
import com.air.common_service.dto.request.VerifyHoldRequest;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.PaymentResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
import com.air.event.BookingCreatedEvent;
import com.air.event.PaymentFailedEvent;
import com.air.event.PaymentSuccessEvent;
import com.air.orchestrator_service.httpclient.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrchestratorService {

    BookingClient bookingClient;
    PaymentClient paymentClient;
    SeatClient seatClient;
    RedissonClient redissonClient;
    RedisTemplate<String, Object> redisTemplate;
    KafkaTemplate<String, Object> kafkaTemplate;

    String BOOKING_KEY = "booking:pending:";

    @Transactional
    public BookingResponse createBookingAndHold(BookingCreateRequest request) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new AppException(ErrorCode.SEAT_NOT_FOUND);
        }

        HoldSeatRequest holdRequest = HoldSeatRequest.builder()
                .seatIds(request.getSeatIds())
                .build();

        List<SeatResponse> heldSeats = seatClient.holdSeat(holdRequest).getResult();
        if (heldSeats == null || heldSeats.isEmpty()) {
            throw new AppException(ErrorCode.SEAT_HOLD_FAILED);
        }

        BookingResponse booking = bookingClient.booking(request).getResult();

        String redisKey = BOOKING_KEY + booking.getId();
        redisTemplate.opsForValue().set(redisKey, userId, 1, TimeUnit.MINUTES);

        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(booking.getId())
                .userId(userId)
                .flightId(booking.getFlightId())
                .seatIds(request.getSeatIds())
                .bookingStatus(BookingStatus.PENDING.name())
                .build();
        kafkaTemplate.send("booking-created", event);

        return booking;
    }

    @Transactional
    public PaymentResponse pay(String bookingId, PaymentMethod method) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        BookingResponse booking = bookingClient.getBooking(bookingId).getResult();
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }

        if (!booking.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_INVALID_STATUS);
        }

        String redisKey = BOOKING_KEY + bookingId;
        Boolean existed = redisTemplate.hasKey(redisKey);
        if (Boolean.FALSE.equals(existed)) {
            cancelBookingAndSeats(booking);
            throw new AppException(ErrorCode.BOOKING_EXPIRED);
        }

        try {
            VerifyHoldRequest verifySeats = VerifyHoldRequest.builder()
                    .seatIds(booking.getSeats().stream().map(SeatResponse::getId).toList())
                    .userId(userId)
                    .build();

            Boolean ok = seatClient.verifyHold(verifySeats).getResult();
            if (!Boolean.TRUE.equals(ok)) {
                cancelBookingAndSeats(booking);
                throw new AppException(ErrorCode.SEAT_HOLD_EXPIRED);
            }

        } catch (Exception ex) {
            cancelBookingAndSeats(booking);
            throw ex;
        }

        PaymentRequest payReq = PaymentRequest.builder()
                .bookingId(bookingId)
                .paymentMethod(method)
                .build();

        PaymentResponse paymentResponse = paymentClient.pay(payReq).getResult();

        PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                .bookingId(booking.getId())
                .userId(userId)
                .flightId(booking.getFlightId())
                .seatIds(booking.getSeats().stream().map(SeatResponse::getId).toList())
                .totalPrice(paymentResponse.getTotalPrice())
                .paymentMethod(method.name())
                .paymentStatus(paymentResponse.getPaymentStatus().name())
                .bookingStatus(BookingStatus.PAID.name())
                .build();

        kafkaTemplate.send("payment-success", event);

        redisTemplate.delete(redisKey);

        return paymentResponse;
    }

    private void rollbackBookingAndSeats(BookingCreateRequest request) {
        try {
            if (request.getSeatIds() != null && !request.getSeatIds().isEmpty()) {
                HoldSeatRequest cancelSeats = HoldSeatRequest.builder()
                        .seatIds(request.getSeatIds())
                        .build();
                seatClient.cancel(cancelSeats);
            }

            bookingClient.cancelSeat(HoldSeatRequest.builder()
                    .seatIds(request.getSeatIds())
                    .build());

        } catch (Exception e) {
            throw new AppException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private void cancelBookingAndSeats(BookingResponse booking) {
        try {
            if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
                List<String> seatIds = booking.getSeats().stream().map(SeatResponse::getId).toList();

                seatClient.cancel(HoldSeatRequest.builder()
                        .seatIds(seatIds)
                        .build());

                bookingClient.cancelSeat(HoldSeatRequest.builder()
                        .seatIds(seatIds)
                        .build());
            }

            String redisKey = BOOKING_KEY + booking.getId();
            redisTemplate.delete(redisKey);

        } catch (Exception e) {
            throw new AppException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Transactional
    public PaymentResponse cancelBooking(String bookingId) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        BookingResponse booking = bookingClient.getBooking(bookingId).getResult();

        if (!booking.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (booking.getBookingStatus() == BookingStatus.CANCEL ||
                booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new AppException(ErrorCode.BOOKING_INVALID_STATUS);
        }

        String lockKey = "orchestrator:cancel-booking:" + bookingId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                throw new AppException(ErrorCode.SYSTEM_BUSY);
            }

            PaymentRequest req = PaymentRequest.builder()
                    .bookingId(bookingId)
                    .paymentMethod(null)
                    .build();
            PaymentResponse paymentResponse = paymentClient.cancelPayment(req).getResult();

            bookingClient.markCancelled(bookingId);

            List<String> seatIds = booking.getSeats().stream()
                    .map(SeatResponse::getId)
                    .toList();

            seatClient.cancel(HoldSeatRequest.builder()
                    .seatIds(seatIds)
                    .build());

            redisTemplate.delete(BOOKING_KEY + bookingId);

            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .bookingId(booking.getId())
                    .userId(booking.getUserId())
                    .flightId(booking.getFlightId())
                    .seatIds(seatIds)
                    .totalPrice(paymentResponse.getTotalPrice())
                    .paymentMethod(paymentResponse.getPaymentMethod() == null
                            ? null
                            : paymentResponse.getPaymentMethod().name())
                    .paymentStatus(paymentResponse.getPaymentStatus().name())
                    .bookingStatus(BookingStatus.CANCEL.name())
                    .build();

            kafkaTemplate.send("payment-failed", event);

            return paymentResponse;

        } catch (AppException ae) {
            throw ae;

        } catch (Exception ex) {
            throw new AppException(ErrorCode.SYSTEM_ERROR);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


}

