package com.air.common_service.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SeatClass {
    ECONOMY,
    BUSINESS;

    @JsonCreator
    public static SeatClass fromString(String key) {
        return key == null ? null : SeatClass.valueOf(key.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}