package com.aidredaline.backend.domain.runningsession.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TrackReq(
        double lat,
        double lng,
        BigDecimal speed,
        BigDecimal altitude,
        BigDecimal accuracy,
        Instant recordedAt
) {}
