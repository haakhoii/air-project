package com.air.common_service.dto.request;

import com.air.common_service.constants.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookAndPayRequest {
    BookingCreateRequest bookingRequest;
    PaymentMethod paymentMethod;
}