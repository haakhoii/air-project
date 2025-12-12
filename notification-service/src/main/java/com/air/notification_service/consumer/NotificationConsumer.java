package com.air.notification_service.consumer;

import com.air.event.BookingCreatedEvent;
import com.air.event.PaymentFailedEvent;
import com.air.event.PaymentSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @KafkaListener(topics = "payment-success", groupId = "notification-service")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {

        log.info("==============================================");
        log.info("📩 RECEIVED PAYMENT SUCCESS EVENT");
        log.info("Booking ID      : {}", event.getBookingId());
        log.info("User ID         : {}", event.getUserId());
        log.info("Flight ID       : {}", event.getFlightId());
        log.info("Seat IDs        : {}", event.getSeatIds());
        log.info("Total Price     : {}", event.getTotalPrice());
        log.info("Payment Method  : {}", event.getPaymentMethod());
        log.info("Payment Status  : {}", event.getPaymentStatus());
        log.info("Booking Status  : {}", event.getBookingStatus());
        log.info("==============================================");

    }

    @KafkaListener(topics = "payment-failed", groupId = "notification-service")
    public void handlePaymentFailed(PaymentFailedEvent event) {

        log.info("==============================================");
        log.info("❌ RECEIVED PAYMENT FAILED EVENT");
        log.info("Booking ID      : {}", event.getBookingId());
        log.info("User ID         : {}", event.getUserId());
        log.info("Flight ID       : {}", event.getFlightId());
        log.info("Seat IDs        : {}", event.getSeatIds());
        log.info("Total Price     : {}", event.getTotalPrice());
        log.info("Payment Method  : {}", event.getPaymentMethod());
        log.info("Payment Status  : {}", event.getPaymentStatus());
        log.info("Booking Status  : {}", event.getBookingStatus());
        log.info("==============================================");

    }

    @KafkaListener(topics = "booking-created", groupId = "notification-service")
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {

        log.info("==============================================");
        log.info("❌ RECEIVED PAYMENT FAILED EVENT");
        log.info("Booking ID      : {}", event.getBookingId());
        log.info("User ID         : {}", event.getUserId());
        log.info("Flight ID       : {}", event.getFlightId());
        log.info("Seat IDs        : {}", event.getSeatIds());
        log.info("Total Price     : {}", event.getTotalPrice());
        log.info("Payment Method  : {}", event.getPaymentMethod());
        log.info("Payment Status  : {}", event.getPaymentStatus());
        log.info("Booking Status  : {}", event.getBookingStatus());
        log.info("==============================================");

    }

}