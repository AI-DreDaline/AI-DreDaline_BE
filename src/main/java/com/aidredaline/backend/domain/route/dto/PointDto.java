package com.aidredaline.backend.domain.route.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좌표 DTO (위도, 경도)
 * - 지도상의 위치를 나타내는 공통 DTO
 * - 시작점, GPS 포인트 등에서 재사용
 * {
 *   "latitude": 33.4996,
 *   "longitude": 126.5312
 * }
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointDto {

    /**
     * 위도 (Latitude)
     * 범위: -90 ~ 90
     */
    @NotNull(message = "위도는 필수입니다")
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다")
    @Max(value = 90, message = "위도는 90 이하여야 합니다")
    private Double latitude;

    /**
     * 경도 (Longitude)
     * 범위: -180 ~ 180
     */
    @NotNull(message = "경도는 필수입니다")
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다")
    @Max(value = 180, message = "경도는 180 이하여야 합니다")
    private Double longitude;

    /**
     * PostGIS Point 생성을 위한 헬퍼 메서드
     * WKT(Well-Known Text) 형식으로 변환
     * 반환 예시: "POINT(126.5312 33.4996)"
     * 주의: PostGIS는 경도(longitude)먼저, 위도(latitude) 나중
     */
    public String toWkt() {
        return String.format("POINT(%f %f)", longitude, latitude);
    }
}