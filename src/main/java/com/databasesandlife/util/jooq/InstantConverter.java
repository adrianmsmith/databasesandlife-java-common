package com.databasesandlife.util.jooq;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.jooq.Converter;

@SuppressWarnings("serial")
public class InstantConverter implements Converter<LocalDateTime, Instant> {

    @Override public Class<LocalDateTime> fromType() { return LocalDateTime.class; }
    @Override public Class<Instant> toType() { return Instant.class; }

    @Override
    public Instant from(LocalDateTime when) {
        if (when == null) return null;
        return when.atZone(ZoneId.of("UTC")).toInstant();
    }

    @Override
    public LocalDateTime to(Instant when) {
        if (when == null) return null;
        return when.atZone(ZoneId.of("UTC")).toLocalDateTime();
    }
}
