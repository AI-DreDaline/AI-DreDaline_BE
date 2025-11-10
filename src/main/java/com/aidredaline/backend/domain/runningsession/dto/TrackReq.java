package com.aidredaline.backend.domain.runningsession.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "GPS 트래킹 요청")
public record TrackReq(
        @Schema(description = "현재 위도", example = "33.5000", required = true)
        double lat,

        @Schema(description = "현재 경도", example = "126.5320", required = true)
        double lng,

        @Schema(description = "현재 속도 (m/s)", example = "3.5")
        BigDecimal speed,

        @Schema(description = "고도 (m)", example = "10.5")
        BigDecimal altitude,

        @Schema(description = "GPS 정확도 (m)", example = "5.0")
        BigDecimal accuracy,

        @Schema(description = "기록 시간 (null이면 현재 시간)", example = "2025-11-10T12:00:00Z")
        Instant recordedAt
) {}
