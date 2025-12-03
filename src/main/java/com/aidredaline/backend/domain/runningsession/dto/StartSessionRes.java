package com.aidredaline.backend.domain.runningsession.dto;

import com.aidredaline.backend.domain.guidance.dto.GuidancePointDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "러닝 시작 응답")
public record StartSessionRes(
        @Schema(description = "생성된 세션 ID", example = "5")
        Integer sessionId,

        @Schema(description = "세션 상태", example = "in_progress")
        String status,

        @Schema(description = "시작 시간", example = "2025-11-10T12:00:00Z")
        Instant startTime,

        @Schema(description = "음성 안내 지점 목록")
        List<GuidancePointDto> guidancePoints

) {}
