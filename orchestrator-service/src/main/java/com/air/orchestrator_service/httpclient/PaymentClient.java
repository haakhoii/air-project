package com.air.orchestrator_service.httpclient;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.PaymentRequest;
import com.air.common_service.dto.response.PaymentResponse;
import com.air.orchestrator_service.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${app.services.payments}", configuration = FeignClientConfig.class)
public interface PaymentClient {
    @PostMapping("/pay")
    ApiResponse<PaymentResponse> pay(@RequestBody PaymentRequest request);
}
