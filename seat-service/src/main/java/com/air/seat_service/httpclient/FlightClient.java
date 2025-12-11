package com.air.seat_service.httpclient;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.response.FlightResponse;
import com.air.seat_service.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "flight-service",
        url = "${app.services.flights}",
        configuration = FeignClientConfig.class
)
public interface FlightClient {
    @GetMapping("/get-by-id/{flightId}")
    ApiResponse<FlightResponse> getFlightById(@PathVariable("flightId") String flightId);
}
