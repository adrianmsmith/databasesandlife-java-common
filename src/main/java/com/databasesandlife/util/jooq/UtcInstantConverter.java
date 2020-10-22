package com.databasesandlife.util.jooq;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.jooq.Converter;

/**
 * @deprecated Prefer TIMEZONETZ type in PostgreSQL which jOOQ automatically recognizes as Instant.
 * And note that this class has issues in jOOQ 3.13 when using .update(..., val(..)). (but 3.11 is OK)
 */
@SuppressWarnings("serial")
public class UtcInstantConverter implements Converter<LocalDateTime, Instant> {

    @Override public Class<LocalDateTime> fromType() { return LocalDateTime.class; }
    @Override public Class<Instant> toType() { return Instant.class; }

    @Override
    public Instant from(LocalDateTime when) {
        if (when == null) return null;
        return when.toInstant(ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime to(Instant when) {
        if (when == null) return null;
        return when.atOffset(ZoneOffset.UTC).toLocalDateTime();
    }
}
