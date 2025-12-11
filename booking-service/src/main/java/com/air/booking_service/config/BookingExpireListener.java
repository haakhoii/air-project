package com.air.booking_service.config;

import com.air.booking_service.service.BookingService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class BookingExpireListener extends KeyExpirationEventMessageListener {

    private final BookingService bookingService;

    public BookingExpireListener(RedisMessageListenerContainer listenerContainer,
                                 BookingService bookingService) {
        super(listenerContainer);
        this.bookingService = bookingService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String expiredKey = message.toString();

        if (!expiredKey.startsWith("booking:pending:")) {
            return;
        }

        String bookingId = expiredKey.replace("booking:pending:", "");

        bookingService.releaseBooking(bookingId);
    }
}
