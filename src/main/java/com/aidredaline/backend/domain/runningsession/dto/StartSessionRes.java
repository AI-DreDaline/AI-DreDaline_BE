package com.aidredaline.backend.domain.runningsession.dto;

import java.time.Instant;

public record StartSessionRes(
        Integer sessionId,
        String status,
        Instant startTime
) {}
