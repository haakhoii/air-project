package com.air.orchestrator_service.service;

import com.air.common_service.constants.BookingStatus;
import com.air.common_service.constants.PaymentMethod;
import com.air.common_service.dto.request.BookingCreateRequest;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.request.PaymentRequest;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.PaymentResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
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
    String PAYMENT_SUCCESS ="payment-success";

    @Transactional
    public PaymentResponse bookAndPay(BookingCreateRequest request, PaymentMethod method) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String lockKey = "orchestrator:booking:" + request.getFlightId() + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(10, 60, TimeUnit.SECONDS)) {
                throw new AppException(ErrorCode.SYSTEM_BUSY);
            }

            BookingResponse booking = bookingClient.booking(request).getResult();

            String redisKey = BOOKING_KEY + booking.getId();
            Boolean existed = redisTemplate.hasKey(redisKey);
            if (Boolean.FALSE.equals(existed)) {
                cancelBookingAndSeats(booking);
                throw new AppException(ErrorCode.BOOKING_EXPIRED);
            }

            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .bookingId(booking.getId())
                    .paymentMethod(method)
                    .build();

            PaymentResponse paymentResponse = paymentClient.pay(paymentRequest).getResult();

            PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                    .bookingId(booking.getId())
                    .userId(userId)
                    .flightId(booking.getFlightId())
                    .seatIds(booking.getSeats().stream().map(SeatResponse::getId).toList())
                    .totalPrice(paymentResponse.getTotalPrice())
                    .paymentMethod(paymentResponse.getPaymentMethod().name())
                    .build();
            kafkaTemplate.send(PAYMENT_SUCCESS, event).get();
            log.info("Emitted payment-success event for booking: {}", booking.getId());

            redisTemplate.delete(redisKey);

            return paymentResponse;

        } catch (AppException ae) {
            if (ae.getErrorCode() != ErrorCode.SYSTEM_BUSY) {
                rollbackBookingAndSeats(request);
            }
            throw ae;

        } catch (Exception ex) {
            rollbackBookingAndSeats(request);
            throw new AppException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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

        BookingResponse booking = bookingClient.getBooking(bookingId).getResult();

        if (booking.getBookingStatus() == BookingStatus.CANCEL ||
                booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new AppException(ErrorCode.BOOKING_INVALID_STATUS);
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


        String redisKey = BOOKING_KEY + bookingId;
        redisTemplate.delete(redisKey);

        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .flightId(booking.getFlightId())
                .seatIds(seatIds)
                .totalPrice(paymentResponse.getTotalPrice())
                .paymentMethod(paymentResponse.getPaymentMethod() == null
                        ? null
                        : paymentResponse.getPaymentMethod().name())
                .build();

        kafkaTemplate.send("payment-failed", event);

        return paymentResponse;
    }

}

