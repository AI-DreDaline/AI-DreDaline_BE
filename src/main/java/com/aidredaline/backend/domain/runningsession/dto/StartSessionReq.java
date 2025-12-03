package com.aidredaline.backend.domain.runningsession.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StartSessionReq(
        @Schema(description = "사용자 ID", example = "1", required = true)
        Integer userId,

        @Schema(description = "경로 ID (생성된 경로 기반)", example = "1", required = true)
        Integer routeId,

        @Schema(description = "시작 위도", example = "33.4996", required = true)
        double startLat,

        @Schema(description = "시작 경도", example = "126.5312", required = true)
        double startLng
) {}
