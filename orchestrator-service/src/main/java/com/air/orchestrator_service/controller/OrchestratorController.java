package com.air.orchestrator_service.controller;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.BookAndPayRequest;
import com.air.common_service.dto.response.PaymentResponse;
import com.air.orchestrator_service.service.OrchestratorService;
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
public class OrchestratorController {

    OrchestratorService orchestratorService;

    @PostMapping("/payment")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PaymentResponse> bookAndPay(
            @RequestBody BookAndPayRequest request
    ) {
        PaymentResponse result = orchestratorService.bookAndPay(request.getBookingRequest(), request.getPaymentMethod());
        return ApiResponse.<PaymentResponse>builder()
                .result(result)
                .build();
    }
}

