package com.air.common_service.dto.request;

import com.air.common_service.constants.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {
    String bookingId;
    PaymentMethod paymentMethod;
}
