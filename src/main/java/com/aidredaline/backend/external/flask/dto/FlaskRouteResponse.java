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

        //음성 안내 데이터
        @JsonProperty("guidance")
        private FlaskGuidanceDto guidance;

        /**
         * GeoJSON에서 좌표 추출하는 헬퍼 메서드
         * Flask가 final_points를 보내지 않을 때 GeoJSON에서 추출
         */
        @SuppressWarnings("unchecked")
        public List<List<Double>> extractCoordinatesFromGeoJson() {
            if (geojson == null) {
                return null;
            }

            try {
                // geojson.features[0].geometry.coordinates 추출
                List<Map<String, Object>> features = (List<Map<String, Object>>) geojson.get("features");
                if (features == null || features.isEmpty()) {
                    return null;
                }

                Map<String, Object> firstFeature = features.get(0);
                Map<String, Object> geometry = (Map<String, Object>) firstFeature.get("geometry");
                if (geometry == null) {
                    return null;
                }

                return (List<List<Double>>) geometry.get("coordinates");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * 최종 좌표 가져오기 (final_points 우선, 없으면 GeoJSON에서 추출)
         */
        public List<List<Double>> getActualCoordinates() {
            if (finalPoints != null && !finalPoints.isEmpty()) {
                return finalPoints;
            }
            return extractCoordinatesFromGeoJson();
        }

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

    /**
     * Flask Guidance DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlaskGuidanceDto {

        @JsonProperty("guidance_points")
        private List<GuidancePoint> guidancePoints;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class GuidancePoint {

            @JsonProperty("sequence")
            private Integer sequence;

            @JsonProperty("type")
            private String type;

            @JsonProperty("lat")
            private Double lat;

            @JsonProperty("lng")
            private Double lng;

            @JsonProperty("direction")
            private String direction;

            @JsonProperty("angle")
            private Double angle;

            @JsonProperty("distance_from_start")
            private Double distanceFromStart;

            @JsonProperty("distance_to_next")
            private Double distanceToNext;

            @JsonProperty("guidance_id")
            private String guidanceId;

            @JsonProperty("trigger_distance")
            private Integer triggerDistance;
        }
    }
}
