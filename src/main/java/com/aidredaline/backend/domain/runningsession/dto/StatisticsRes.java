package com.aidredaline.backend.domain.runningsession.dto;

import java.math.BigDecimal;

public record StatisticsRes(
        int totalRuns,
        BigDecimal totalDistance,
        BigDecimal averagePace
) {}
