package com.aidredaline.backend.domain.runningsession.dto;

public record StartSessionReq(
        Long userId,
        Long routeId,
        double startLat,
        double startLng
) {}
