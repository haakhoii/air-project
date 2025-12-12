package com.air.orchestrator_service.httpclient;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.request.VerifyHoldRequest;
import com.air.common_service.dto.response.SeatResponse;
import com.air.orchestrator_service.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "seat-service", url = "${app.services.seats}", configuration = FeignClientConfig.class)
public interface SeatClient {
    @PostMapping("/internal/hold")
    ApiResponse<List<SeatResponse>> holdSeat(@RequestBody HoldSeatRequest request);

    @PostMapping("/internal/cancel")
    ApiResponse<List<SeatResponse>> cancel(@RequestBody HoldSeatRequest request);

    @PostMapping("/internal/verify-hold")
    ApiResponse<Boolean> verifyHold(@RequestBody VerifyHoldRequest request);
}
