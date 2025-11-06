package com.aidredaline.backend.external.flask.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Flask 서버로 보내는 경로 생성 요청 DTO
 *
 * Flask API
 * POST /routes/generate
 * {
 *   "start_point": {"lat": 33.4996, "lng": 126.5312},
 *   "target_km": 5.0,
 *   "template_name": "heart.svg"
 * }
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaskRouteRequest {


    @JsonProperty("start_point")
    private FlaskPointDto startPoint;

    @JsonProperty("target_km")
    private Double targetKm;

    @JsonProperty("template_name")
    private String templateName;

    @JsonProperty("options")
    private FlaskOptionsDto options;


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlaskPointDto {
        @JsonProperty("lat")
        private Double lat;

        @JsonProperty("lng")
        private Double lng;
    }

    /**
     * Flask Options DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlaskOptionsDto {
        @JsonProperty("map_match")
        private Boolean mapMatch;

        @JsonProperty("rotation_deg")
        private Double rotationDeg;
    }
}