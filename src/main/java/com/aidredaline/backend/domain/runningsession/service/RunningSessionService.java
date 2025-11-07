package com.aidredaline.backend.domain.runningsession.service;

import com.aidredaline.backend.domain.runningsession.dto.*;
import com.aidredaline.backend.domain.runningsession.entity.GpsTrackingPoint;
import com.aidredaline.backend.domain.runningsession.entity.RunningSession;
import com.aidredaline.backend.domain.runningsession.geo.GeoFactory;
import com.aidredaline.backend.domain.runningsession.repository.GpsTrackingPointRepository;
import com.aidredaline.backend.domain.runningsession.repository.RunningSessionRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * RunningSessionService
 * ---------------------
 * - 1️⃣ 러닝 시작
 * - 2️⃣ GPS 트래킹 저장
 * - 3️⃣ 일시정지 / 재개
 * - 4️⃣ 러닝 완료 (거리/페이스/칼로리 계산)
 * - 5️⃣ 상세/목록/통계 조회
 * - 6️⃣ GPS 포인트 목록 조회 (지도용)
 */
@Service
@RequiredArgsConstructor
public class RunningSessionService {

    private final RunningSessionRepository sessionRepo;
    private final GpsTrackingPointRepository gpsRepo;
    private final GeoFactory geo;

    // 1️⃣ 러닝 세션 시작
    @Transactional
    public StartSessionRes start(StartSessionReq req) {
        RunningSession s = new RunningSession();
        s.setUserId(req.userId());
        s.setRouteId(req.routeId());
        s.setStartTime(Instant.now());
        s.setStatus("in_progress");
        s.setCurrentPosition(geo.point(req.startLat(), req.startLng()));
        sessionRepo.save(s);
        return new StartSessionRes(s.getSessionId(), s.getStatus(), s.getStartTime());
    }

    // 2️⃣ GPS 트래킹 데이터 저장
    @Transactional
    public void track(Long sessionId, TrackReq req) {
        RunningSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!"in_progress".equals(s.getStatus()) && !"paused".equals(s.getStatus())) {
            throw new IllegalStateException("Session not active");
        }

        GpsTrackingPoint p = new GpsTrackingPoint();
        p.setSessionId(sessionId);
        p.setLocation(geo.point(req.lat(), req.lng()));
        p.setRecordedAt(Optional.ofNullable(req.recordedAt()).orElse(Instant.now()));
        p.setSpeed(req.speed());
        p.setAltitude(req.altitude());
        p.setAccuracy(req.accuracy());

        gpsRepo.save(p);
        s.setCurrentPosition(p.getLocation());
    }

    // 3️⃣ 러닝 일시정지
    @Transactional
    public void pause(Long sessionId) {
        RunningSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!"in_progress".equals(s.getStatus()))
            throw new IllegalStateException("Not in progress");

        List<Map<String, Object>> history = Optional.ofNullable(s.getPauseHistory()).orElse(new ArrayList<>());
        history.add(Map.of("pauseAt", Instant.now().toString()));

        s.setPauseHistory(history);
        s.setStatus("paused");
        sessionRepo.save(s);
    }

    // 3️⃣ 러닝 재개
    @Transactional
    public void resume(Long sessionId) {
        RunningSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!"paused".equals(s.getStatus()))
            throw new IllegalStateException("Not paused");

        List<Map<String, Object>> history = Optional.ofNullable(s.getPauseHistory()).orElse(new ArrayList<>());
        for (int i = history.size() - 1; i >= 0; i--) {
            Map<String, Object> last = history.get(i);
            if (!last.containsKey("resumeAt")) {
                Instant pauseAt = Instant.parse((String) last.get("pauseAt"));
                Instant resumeAt = Instant.now();
                last.put("resumeAt", resumeAt.toString());
                int pausedSeconds = (int) Duration.between(pauseAt, resumeAt).getSeconds();
                s.setTotalPausedDuration(Optional.ofNullable(s.getTotalPausedDuration()).orElse(0) + pausedSeconds);
                break;
            }
        }

        s.setPauseHistory(history);
        s.setStatus("in_progress");
        sessionRepo.save(s);
    }

    // 4️⃣ 러닝 완료 및 분석 (거리, 페이스, 칼로리)
    @Transactional
    public CompleteSessionRes complete(Long sessionId) {
        RunningSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!List.of("in_progress", "paused").contains(s.getStatus()))
            throw new IllegalStateException("Session must be active or paused");

        Instant endTime = Instant.now();
        s.setEndTime(endTime);
        s.setStatus("completed");

        var points = gpsRepo.findBySessionIdOrderByRecordedAtAsc(sessionId);
        BigDecimal totalDistance = BigDecimal.ZERO;
        for (int i = 1; i < points.size(); i++)
            totalDistance = totalDistance.add(BigDecimal.valueOf(distance(points.get(i - 1).getLocation(), points.get(i).getLocation())));

        long movingSeconds = Duration.between(s.getStartTime(), endTime).getSeconds()
                - Optional.ofNullable(s.getTotalPausedDuration()).orElse(0);
        s.setMovingTime((int) movingSeconds);

        BigDecimal minutes = BigDecimal.valueOf(movingSeconds).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal km = totalDistance.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        BigDecimal pace = km.compareTo(BigDecimal.ZERO) > 0 ? minutes.divide(km, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        int calories = km.multiply(BigDecimal.valueOf(60)).intValue();
        s.setCalories(calories);
        s.setActualDistance(totalDistance);
        s.setAveragePace(pace);

        sessionRepo.save(s);
        return new CompleteSessionRes(s.getSessionId(), s.getStartTime(), s.getEndTime(), totalDistance, pace, calories);
    }

    // 내부 거리 계산 (Haversine)
    private double distance(Point p1, Point p2) {
        double R = 6371000;
        double lat1 = Math.toRadians(p1.getY());
        double lat2 = Math.toRadians(p2.getY());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(p2.getX() - p1.getX());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // 5️⃣ 상세 조회
    @Transactional(readOnly = true)
    public SessionDetailRes getDetail(Long sessionId) {
        RunningSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        return new SessionDetailRes(s.getSessionId(), s.getStartTime(), s.getEndTime(),
                s.getActualDistance(), s.getAveragePace(), s.getCalories(), s.getStatus());
    }

    // 5️⃣ 목록 조회
    @Transactional(readOnly = true)
    public Page<SessionItemRes> getList(Long userId, int page, int size) {
        return sessionRepo.findByUserIdAndStatusOrderByStartTimeDesc(userId, "completed", PageRequest.of(page, size))
                .map(s -> new SessionItemRes(s.getSessionId(), s.getStartTime(), s.getEndTime(),
                        s.getActualDistance(), s.getAveragePace(), s.getCalories()));
    }

    // 5️⃣ 통계 조회
    @Transactional(readOnly = true)
    public StatisticsRes getStatistics(Long userId) {
        Object[] result = sessionRepo.getStatisticsSummary(userId);
        return new StatisticsRes(((Number) result[0]).intValue(),
                BigDecimal.valueOf(((Number) result[1]).doubleValue()),
                BigDecimal.valueOf(((Number) result[2]).doubleValue()));
    }

    // 6️⃣ GPS 포인트 목록 조회 (지도 시각화용)
    @Transactional(readOnly = true)
    public List<GpsPointRes> getPoints(Long sessionId) {
        List<GpsTrackingPoint> points = gpsRepo.findBySessionIdOrderByRecordedAtAsc(sessionId);
        return points.stream()
                .map(p -> new GpsPointRes(
                        p.getLocation().getY(),  // lat
                        p.getLocation().getX(),  // lng
                        p.getSpeed(),
                        p.getAltitude(),
                        p.getAccuracy(),
                        p.getRecordedAt()
                ))
                .toList();
    }
}
