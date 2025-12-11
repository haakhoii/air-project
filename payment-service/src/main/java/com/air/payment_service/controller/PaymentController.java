package com.air.payment_service.controller;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.PaymentRequest;
import com.air.common_service.dto.response.PaymentResponse;
import com.air.payment_service.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;

    @PostMapping("/pay")
    @PreAuthorize("hasRole('USER')")
    ApiResponse<PaymentResponse> pay(@RequestBody PaymentRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.pay(request))
                .build();
    }
}
