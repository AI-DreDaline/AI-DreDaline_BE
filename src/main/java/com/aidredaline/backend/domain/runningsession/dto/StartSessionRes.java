package com.aidredaline.backend.domain.runningsession.dto;

import java.time.Instant;

public record StartSessionRes(
        Long sessionId,
        String status,
        Instant startTime
) {}
