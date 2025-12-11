package com.air.common_service.config;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeConfig {

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // input
    public Instant parse(String value) {
        LocalDateTime ldt = LocalDateTime.parse(value, formatter);
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }

    // output
    public String format(Instant instant) {
        LocalDateTime ldt = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return ldt.format(formatter);
    }
}
