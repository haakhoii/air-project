package com.air.payment_service.service;

import com.air.common_service.constants.BookingStatus;
import com.air.common_service.constants.PaymentMethod;
import com.air.common_service.constants.PaymentStatus;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.request.PaymentRequest;
import com.air.common_service.dto.response.BookingResponse;
import com.air.common_service.dto.response.PaymentResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
import com.air.payment_service.entity.Payment;
import com.air.payment_service.httpclient.BookingClient;
import com.air.payment_service.httpclient.SeatClient;
import com.air.payment_service.repository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    BookingClient bookingClient;
    SeatClient seatClient;
    PaymentRepository paymentRepository;
    RedisTemplate<String, Object> redisTemplate;

    String BOOKING_KEY = "booking:pending:";

    @Transactional
    public PaymentResponse pay(PaymentRequest paymentRequest) {

        String bookingId = paymentRequest.getBookingId();
        PaymentMethod method = paymentRequest.getPaymentMethod();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        String redisKey = BOOKING_KEY + bookingId;
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (Boolean.FALSE.equals(exists)) {
            throw new AppException(ErrorCode.BOOKING_EXPIRED);
        }

        BookingResponse booking = bookingClient
                .getBookingById(bookingId)
                .getResult();

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.BOOKING_NOT_PENDING);
        }

        boolean paymentSuccess = true;
        if (!paymentSuccess) {
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        }

        List<String> seatIds = booking.getSeats()
                .stream()
                .map(SeatResponse::getId)
                .collect(Collectors.toList());

        HoldSeatRequest seatReq = HoldSeatRequest.builder()
                .seatIds(seatIds)
                .build();

        seatClient.bookSeats(seatReq);

        bookingClient.markPaid(bookingId);

        redisTemplate.delete(redisKey);

        Payment payment = paymentRepository.save(
                Payment.builder()
                        .bookingId(bookingId)
                        .userId(userId)
                        .totalPrice(booking.getTotalPrice())
                        .paymentMethod(method)
                        .status(PaymentStatus.SUCCESS)
                        .createdAt(Instant.now())
                        .build()
        );

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(bookingId)
                .userId(userId)
                .totalPrice(payment.getTotalPrice())
                .paymentMethod(method)
                .paymentStatus(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
