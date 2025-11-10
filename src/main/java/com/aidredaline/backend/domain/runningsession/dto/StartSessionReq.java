package com.aidredaline.backend.domain.runningsession.dto;

public record StartSessionReq(
        Integer userId,
        Integer routeId,
        double startLat,
        double startLng
) {}
