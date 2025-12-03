package com.aidredaline.backend.domain.runningsession.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisRes(
        Integer sessionId,
        BigDecimal totalDistance,
        BigDecimal averagePace,
        BigDecimal maxSpeed,
        BigDecimal avgSpeed,
        Integer totalCalories,
        List<KmSplit> splits
) {
    public record KmSplit(
            int kmIndex,
            BigDecimal segmentDistance,
            BigDecimal segmentPace,
            int segmentCalories
    ) {}
}
