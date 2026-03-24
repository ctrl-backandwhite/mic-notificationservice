package com.backandwhite.provider;

import java.time.Instant;

public final class AuditProvider {

    public static final Instant CREATED_AT = Instant.parse("2025-01-01T00:00:00Z");
    public static final Instant UPDATED_AT = Instant.parse("2025-01-02T00:00:00Z");
    public static final String CREATED_BY = "system";
    public static final String UPDATED_BY = "admin";

    private AuditProvider() {
    }
}
