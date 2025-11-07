package com.aidredaline.backend.external.flask.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Flask 서버로부터 받는 경로 생성 응답 DTO
 * {
 *   "ok": true,
 *   "data": {
 *     "geojson": {...},
 *     "metrics": {
 *       "target_km": 5.0,
 *       "route_length_m": 5200.5,
 *       "nodes": 150,
 *       "scale_m_per_unit": 50.0
 *     },
 *     "template_points": [[lng, lat], ...],
 *     "route_points": [[lng, lat], ...],
 *     "final_points": [[lng, lat], ...]
 *   }
 * }
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaskRouteResponse {
    @JsonProperty("ok")
    private Boolean ok;

    @JsonProperty("data")
    private FlaskDataDto data;

    @JsonProperty("error")
    private FlaskErrorDto error;



    /**
     * Flask Data DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlaskDataDto {

        /**
         * GeoJSON (시각화용)
         */
        @JsonProperty("geojson")
        private Map<String, Object> geojson;

        /**
         * 메트릭 정보
         */
        @JsonProperty("metrics")
        private FlaskMetricsDto metrics;

        //템플릿 원본 좌표
        @JsonProperty("template_points")
        private List<List<Double>> templatePoints;

        // 맵매칭 전 경로 좌표
        @JsonProperty("route_points")
        private List<List<Double>> routePoints;

        //맵매칭 후 최종 경로
        @JsonProperty("final_points")
        private List<List<Double>> finalPoints;
    }

    /**
     * Flask Metrics DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlaskMetricsDto {

        @JsonProperty("target_km")
        private Double targetKm;

        @JsonProperty("route_length_m")
        private Double routeLengthM;

        @JsonProperty("nodes")
        private Integer nodes;

        @JsonProperty("scale_m_per_unit")
        private Double scaleMPerUnit;
    }

    /**
     * Flask Error DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlaskErrorDto {
        @JsonProperty("code")
        private Integer code;

        @JsonProperty("message")
        private String message;
    }
}
