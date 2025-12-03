package com.aidredaline.backend.domain.runningsession.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "러닝 세션 상세 정보")
public record SessionDetailRes(
        @Schema(description = "세션 ID", example = "1")
        Integer sessionId,

        @Schema(description = "시작 시간", example = "2025-11-09T10:00:00Z")
        Instant startTime,

        @Schema(description = "종료 시간", example = "2025-11-09T10:35:00Z")
        Instant endTime,

        @Schema(description = "실제 이동 거리 (미터)", example = "5020.5")
        BigDecimal totalDistance,

        @Schema(description = "평균 페이스 (분/km)", example = "5.98")
        BigDecimal averagePace,

        @Schema(description = "소모 칼로리 (kcal)", example = "300")
        Integer calories,

        @Schema(description = "세션 상태", example = "completed")
        String status
) {}
