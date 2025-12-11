package com.air.api_gateway.service;

import com.air.api_gateway.httpclient.UserClient;
import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.IntrospectRequest;
import com.air.common_service.dto.response.IntrospectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserClient userClient;

    public Mono<ApiResponse<IntrospectResponse>> introspect(String accessToken) {
        return userClient.introspect(IntrospectRequest.builder()
                .accessToken(accessToken)
                .build());
    }
}
