package com.air.user_service.service;

import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
import com.air.user_service.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtToken {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.expiryMinutes}")
    private long EXPIRY_MINUTES;

    @Value("${jwt.expiryDays}")
    private long EXPIRY_DAYS;

    public String generateAccessToken(User user) {
        return generateToken(user, EXPIRY_MINUTES, ChronoUnit.MINUTES);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, EXPIRY_DAYS, ChronoUnit.DAYS);
    }

    private String generateToken(User user, long expiry, ChronoUnit chronoUnit) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        StringJoiner scope = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role ->
                    scope.add("ROLE_" + role.getName())
            );
        }

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("HAK")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(expiry, chronoUnit).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("username", user.getUsername())
                .claim("scope", scope.toString())
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject object = new JWSObject(header, payload);

        try {
            object.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return object.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public Instant generateExpiryAccess() {
        return Instant.now().plus(EXPIRY_MINUTES, ChronoUnit.MINUTES);
    }

    public Instant generateExpiryRefresh() {
        return Instant.now().plus(EXPIRY_DAYS, ChronoUnit.DAYS);
    }

    public SignedJWT verify(String token, boolean isValid) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        if (!signedJWT.verify(verifier))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        if (isValid) {
            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (exp.before(new Date()))
                throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        boolean isBlacklisted =
                Boolean.TRUE.equals(redisTemplate.hasKey("invalid_token:access:" + jwtId)) ||
                Boolean.TRUE.equals(redisTemplate.hasKey("invalid_token:refresh:" + jwtId));
        if (isBlacklisted)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }
}
