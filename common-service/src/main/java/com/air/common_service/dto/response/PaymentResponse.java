package com.air.common_service.dto.response;

import com.air.common_service.constants.PaymentMethod;
import com.air.common_service.constants.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    String paymentId;
    String bookingId;
    String userId;
    Double totalPrice;
    PaymentMethod paymentMethod;
    PaymentStatus paymentStatus;
    Instant createdAt;
}

