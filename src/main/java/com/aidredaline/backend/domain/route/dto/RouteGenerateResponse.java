package com.aidredaline.backend.domain.route.dto;

import com.aidredaline.backend.domain.route.entity.GeneratedRoute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 경로 생성 응답 DTO
 * - 생성된 경로 정보를 프론트에 전달
 * - 지도에 경로를 그리기 위한 좌표 배열 제공
 *
 * POST /api/routes/generate 응답
 * GET /api/routes/{id} 응답
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "경로 생성 응답")
public class RouteGenerateResponse {


    @Schema(description = "경로 ID", example = "123")
    private Integer routeId;

    @Schema(description = "템플릿 ID", example = "1")
    private Integer templateId;

    @Schema(description = "템플릿 이름", example = "star")
    private String templateName;

    @Schema(description = "총 거리 (km)", example = "5.2")
    private Double totalDistance;

    @Schema(description = "예상 소요 시간 (초)", example = "1800")
    private Integer expectedDuration;

    @Schema(description = "템플릿 유사도 점수", example = "0.92")
    private Double similarityScore;

    /**
     * 경로 좌표 배열
     * 프론트에서 지도에 선을 그리기 위한 좌표들
     */
    @Schema(description = "경로 좌표 배열")
    private List<PointDto> routePath;

    @Schema(description = "생성 시각", example = "2025-11-05T23:10:00")
    private LocalDateTime createdAt;


    public static RouteGenerateResponse from(GeneratedRoute route, String templateName) {
        return RouteGenerateResponse.builder()
                .routeId(route.getRouteId())
                .templateId(route.getTemplateId())
                .templateName(templateName)
                .totalDistance(route.getTotalDistance())
                .expectedDuration(route.getExpectedDuration())
                .similarityScore(route.getSimilarityScore())
                .routePath(convertLineStringToPoints(route.getRoutePath()))
                .createdAt(route.getCreatedAt())
                .build();
    }

    /**
     * LineString의 각 좌표를 PointDto 리스트로 변환
     * @param lineString PostGIS LineString
     * @return 좌표 리스트
     */
    private static List<PointDto> convertLineStringToPoints(LineString lineString) {
        if (lineString == null) {
            return new ArrayList<>();
        }

        List<PointDto> points = new ArrayList<>();
        Coordinate[] coordinates = lineString.getCoordinates();

        for (Coordinate coord : coordinates) {
            points.add(PointDto.builder()
                    .longitude(coord.x)  // PostGIS는 x = 경도
                    .latitude(coord.y)   // PostGIS는 y = 위도
                    .build());
        }

        return points;
    }
}
