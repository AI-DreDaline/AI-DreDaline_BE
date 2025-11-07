package com.aidredaline.backend.domain.runningsession.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SessionItemRes(
        Integer sessionId,
        Instant startTime,
        Instant endTime,
        BigDecimal totalDistance,
        BigDecimal averagePace,
        Integer calories
) {}
