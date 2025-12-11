package com.air.common_service.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    MOMO,
    BANK_TRANSFER,
    CASH;

    @JsonCreator
    public static PaymentMethod fromString(String key) {
        return key == null ? null : PaymentMethod.valueOf(key.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}