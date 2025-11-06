package com.aidredaline.backend.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 경로 생성 요청 DTO
 * - 사용자가 경로 생성을 요청할 때 필요한 정보
 * - Flask AI 서버에 전달할 파라미터
 *
 * POST /api/routes/generate 에서 쓸거임
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "경로 생성 요청")
public class RouteGenerateRequest {

    @NotNull(message = "템플릿 ID는 필수입니다")
    @Schema(description = "템플릿 ID", example = "1", required = true)
    private Integer templateId;

    @NotNull(message = "시작 지점은 필수입니다")
    @Valid
    @Schema(description = "시작 지점", required = true)
    private PointDto startPoint;

    @NotNull(message = "목표 거리는 필수입니다")
    @Min(value = 1, message = "목표 거리는 최소 1km 이상이어야 합니다")
    @Schema(description = "목표 거리 (km)", example = "5.0", required = true)
    private Double targetDistance;
}