package com.aidredaline.backend.domain.guidance.dto;

import com.aidredaline.backend.external.flask.dto.FlaskRouteResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "음성 안내 포인트")
public class GuidancePointDto {

    @Schema(description = "턴바이턴 순서", example = "1")
    private Integer sequence;

    @Schema(description = "위도", example = "33.4996")
    private Double lat;

    @Schema(description = "경도", example = "126.5312")
    private Double lng;

    @Schema(description = "방향 (left/right/straight)", example = "left")
    private String direction;

    @Schema(description = "회전 각도", example = "-92.7")
    private Double angle;

    @Schema(description = "출발점으로부터 거리 (m)", example = "44.4")
    private BigDecimal distanceFromStart;

    @Schema(description = "다음 지점까지 거리 (m)", example = "104.4")
    private BigDecimal distanceToNext;

    @Schema(description = "안내 ID (TURN_LEFT_50 등)", example = "TURN_LEFT_50")
    private String guidanceId;

    @Schema(description = "안내 텍스트", example = "50미터 앞에서 좌회전하세요")
    private String guidanceText;

    @Schema(description = "TTS 음성 파일 URL", example = "http://localhost:8080/tts/turn_left_50.mp3")
    private String ttsUrl;

    @Schema(description = "안내 트리거 거리 (m)", example = "50")
    private Double triggerDistance;

    @Schema(description = "안내 타입 (turn/progress/event/checkpoint)", example = "turn")
    private String type;

    /**
     * Flask GuidancePoint → DTO 변환
     */
    public static GuidancePointDto from(
            FlaskRouteResponse.FlaskGuidanceDto.GuidancePoint flaskPoint,
            String baseUrl
    ) {
        // TTS URL 생성
        String ttsUrl = null;
        String guidanceText = null;

        if (flaskPoint.getGuidanceId() != null) {
            String fileName = flaskPoint.getGuidanceId().toLowerCase() + ".mp3";
            ttsUrl = baseUrl + "/tts/" + fileName;
            guidanceText = mapGuidanceIdToText(flaskPoint.getGuidanceId());
        }

        return GuidancePointDto.builder()
                .sequence(flaskPoint.getSequence())
                .type(flaskPoint.getType())
                .lat(flaskPoint.getLat())
                .lng(flaskPoint.getLng())
                .direction(flaskPoint.getDirection())
                .angle(flaskPoint.getAngle())
                .distanceFromStart(BigDecimal.valueOf(flaskPoint.getDistanceFromStart()))
                .distanceToNext(BigDecimal.valueOf(flaskPoint.getDistanceToNext()))
                .guidanceId(flaskPoint.getGuidanceId())
                .guidanceText(guidanceText)
                .ttsUrl(ttsUrl)
                .triggerDistance(flaskPoint.getTriggerDistance() != null ?
                        flaskPoint.getTriggerDistance().doubleValue() : null)
                .build();
    }

    private static String mapGuidanceIdToText(String guidanceId) {
        return switch (guidanceId) {
            case "TURN_LEFT_15" -> "15미터 앞에서 좌회전하세요";
            case "TURN_LEFT_50" -> "50미터 앞에서 좌회전하세요";
            case "TURN_RIGHT_15" -> "15미터 앞에서 우회전하세요";
            case "TURN_RIGHT_50" -> "50미터 앞에서 우회전하세요";
            case "U_TURN_15" -> "15미터 앞에서 유턴하세요";
            case "U_TURN_50" -> "50미터 앞에서 유턴하세요";
            case "GO_STRAIGHT_NEXT" -> "다음 안내까지 직진하세요";
            case "SLIGHT_LEFT" -> "약간 왼쪽으로 이동하세요";
            case "SLIGHT_RIGHT" -> "약간 오른쪽으로 이동하세요";
            case "SHARP_LEFT" -> "급하게 좌회전하세요";
            case "SHARP_RIGHT" -> "급하게 우회전하세요";
            case "RUN_START" -> "러닝을 시작합니다";
            case "RUN_FINISH" -> "수고하셨습니다. 러닝을 완료했습니다";
            case "PROGRESS_30" -> "30% 지점에 도달했습니다";
            case "PROGRESS_50" -> "절반 지점에 도착했습니다";
            case "PROGRESS_80" -> "80% 지점입니다";
            case "CHECKPOINT_ARRIVED" -> "체크포인트에 도착했습니다";
            case "ROUTE_COMPLETE" -> "경로를 모두 완료했습니다";
            case "OFF_ROUTE" -> "경로에서 벗어났습니다";
            default -> null;
        };
    }
}