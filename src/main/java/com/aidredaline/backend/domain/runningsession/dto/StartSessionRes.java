package com.aidredaline.backend.domain.runningsession.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "러닝 시작 응답")
public record StartSessionRes(
        @Schema(description = "생성된 세션 ID", example = "5")
        Integer sessionId,

        @Schema(description = "세션 상태", example = "in_progress")
        String status,

        @Schema(description = "시작 시간", example = "2025-11-10T12:00:00Z")
        Instant startTime
) {}
