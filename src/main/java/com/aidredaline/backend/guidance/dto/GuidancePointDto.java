package com.aidredaline.backend.guidance.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuidancePointDto {
    private Integer sequence;           // 턴바이턴 순서
    private Double lat;                 // 위도
    private Double lng;                 // 경도
    private String direction;           // "left", "right", "straight"
    private Double angle;               // 회전 각도
    private BigDecimal distanceFromStart;  // 출발점으로부터 거리
    private BigDecimal distanceToNext;     // 다음 지점까지 거리
    private String guidanceId;          // "TURN_LEFT_50"
    private String guidanceText;        // "50미터 앞에서 좌회전하세요"
    private String ttsUrl;              // "http://backend:8080/tts/turn_left_50.mp3"
    private Double triggerDistance;     // 안내 트리거 거리 (15m)
}