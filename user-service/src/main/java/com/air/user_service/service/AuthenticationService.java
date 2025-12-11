package com.air.user_service.service;

import com.air.common_service.config.DateTimeConfig;
import com.air.common_service.dto.request.IntrospectRequest;
import com.air.common_service.dto.request.LoginRequest;
import com.air.common_service.dto.request.LogoutRequest;
import com.air.common_service.dto.response.AuthenticationResponse;
import com.air.common_service.dto.response.IntrospectResponse;
import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
import com.air.user_service.entity.User;
import com.air.user_service.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtToken jwtToken;
    DateTimeConfig dateTimeConfig;
    RedisTemplate<String, Object> redisTemplate;

    public AuthenticationResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);

        String accessToken = jwtToken.generateAccessToken(user);
        String expiryAccess = dateTimeConfig.format(jwtToken.generateExpiryAccess());

        String refreshToken = jwtToken.generateRefreshToken(user);
        String expiryRefresh = dateTimeConfig.format(jwtToken.generateExpiryRefresh());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiryAccess(expiryAccess)
                .expiryRefresh(expiryRefresh)
                .build();
    }

    public String logout(LogoutRequest request) {
        String username = null;
        boolean isValid = true;

        try {
            SignedJWT access = jwtToken.verify(request.getAccessToken(), true);
            username = access.getJWTClaimsSet().getStringClaim("username");
            blacklistToken(access, "access");
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.UNAUTHORIZED) {
                isValid = false;
            }
        } catch (Exception e) {
            isValid = false;
        }

        try {
            SignedJWT refresh = jwtToken.verify(request.getRefreshToken(), true);
            blacklistToken(refresh, "refresh");
        } catch (Exception e) {
           log.warn("Refresh token invalid or already blacklisted");
        }

        if (!isValid) {
            throw new AppException(ErrorCode.ALREADY_LOGGED_OUT);
        }

        return "Logout successful with username: " + username;
    }

    private void blacklistToken(SignedJWT signedJWT, String type) throws ParseException {
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Instant expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();

        long ttl = Duration.between(Instant.now(), expiryTime).getSeconds();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    "invalid_token:" + type + ":" + jwtId,
                    true,
                    ttl,
                    TimeUnit.SECONDS
            );
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        String access = request.getAccessToken();
        boolean isValid = true;
        SignedJWT signedJWT = null;
        String userId = null;
        String username = null;

        try {
            signedJWT = jwtToken.verify(access, true);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        if (signedJWT != null) {
            try {
                userId = signedJWT.getJWTClaimsSet().getSubject();
                username = signedJWT.getJWTClaimsSet().getStringClaim("username");
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .userId(userId)
                .username(username)
                .build();

    }
}
