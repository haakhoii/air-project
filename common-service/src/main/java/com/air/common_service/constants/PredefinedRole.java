package com.air.common_service.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PredefinedRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String roleName;
}
