package com.air.seat_service.config;

import com.air.common_service.constants.SeatStatus;
import com.air.seat_service.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class SeatHoldExpireListener extends KeyExpirationEventMessageListener {

    SeatRepository seatRepository;

    @Autowired
    public SeatHoldExpireListener
            (RedisMessageListenerContainer listenerContainer,
                                      SeatRepository seatRepository) {
        super(listenerContainer);
        this.seatRepository = seatRepository;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (!expiredKey.startsWith("seat:hold:")) {
            return;
        }

        String seatId = expiredKey.replace("seat:hold:", "");

        seatRepository.findById(seatId).ifPresent(seat -> {
            if (seat.getSeatStatus() == SeatStatus.HOLD) {
                seat.setSeatStatus(SeatStatus.AVAILABLE);
                seat.setHoldBy(null);
                seatRepository.save(seat);
            }
        });
    }
}

