package com.aidredaline.backend.domain.runningsession.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 6️⃣ 지도 시각화용 GPS 포인트 응답 DTO
 * - 러닝 중 기록된 위/경도, 속도, 고도, 정확도, 기록 시간 포함
 */
public record GpsPointRes(
        double lat,
        double lng,
        BigDecimal speed,
        BigDecimal altitude,
        BigDecimal accuracy,
        Instant recordedAt
) {}
