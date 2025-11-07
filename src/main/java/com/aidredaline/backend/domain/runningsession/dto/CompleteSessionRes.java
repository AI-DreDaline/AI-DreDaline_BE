package com.aidredaline.backend.domain.runningsession.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CompleteSessionRes(
        Long sessionId,
        Instant startTime,
        Instant endTime,
        BigDecimal totalDistance,   // m 단위
        BigDecimal averagePace,     // 분/km
        Integer calories
) {}
