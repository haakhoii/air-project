package com.air.payment_service.httpclient;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.response.SeatResponse;
import com.air.payment_service.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "seat-service",
        url = "${app.services.seats}",
        configuration = FeignClientConfig.class
)
public interface SeatClient {

    @PostMapping("/internal/book")
    ApiResponse<List<SeatResponse>> bookSeats(@RequestBody HoldSeatRequest request);
}

