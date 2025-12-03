package com.aidredaline.backend.domain.runningsession.service;

import com.aidredaline.backend.domain.route.entity.GeneratedRoute;
import com.aidredaline.backend.domain.guidance.dto.GuidancePointDto;
import com.aidredaline.backend.domain.guidance.service.VoiceGuidanceService;
import com.aidredaline.backend.domain.route.repository.GeneratedRouteRepository;
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
 * - 7️⃣ 완료 목록 페이지네이션 + 요약 통계
 */
@Service
@RequiredArgsConstructor
public class RunningSessionService {

    private final RunningSessionRepository sessionRepo;
    private final GpsTrackingPointRepository gpsRepo;
    private final GeneratedRouteRepository routeRepo;
    private final VoiceGuidanceService voiceGuidanceService;
    private final GeoFactory geo;

    // 1️⃣ 러닝 세션 시작 (음성안내 데이터 포함)
    @Transactional
    public StartSessionRes start(StartSessionReq req) {
        //세션 생성
        RunningSession s = new RunningSession();
        s.setUserId(req.userId());
        s.setRouteId(req.routeId());
        s.setStartTime(Instant.now());
        s.setStatus("in_progress");
        s.setCurrentPosition(geo.point(req.startLat(), req.startLng()));
        sessionRepo.save(s);

        // 음성 안내 데이터 조회
        List<GuidancePointDto> guidancePoints =
                voiceGuidanceService.getGuidancePoints(req.routeId());

        return new StartSessionRes(
                s.getSessionId(),
                s.getStatus(),
                s.getStartTime(),
                guidancePoints
        );
    }

    // 2️⃣ GPS 트래킹 데이터 저장
    @Transactional
    public void track(Integer sessionId, TrackReq req) {
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
    public void pause(Integer sessionId) {
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
    public void resume(Integer sessionId) {
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
    public CompleteSessionRes complete(Integer sessionId) {
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
            totalDistance = totalDistance.add(BigDecimal.valueOf(
                    distance(points.get(i - 1).getLocation(), points.get(i).getLocation())
            ));

        long movingSeconds = Duration.between(s.getStartTime(), endTime).getSeconds()
                - Optional.ofNullable(s.getTotalPausedDuration()).orElse(0);
        s.setMovingTime((int) movingSeconds);

        BigDecimal minutes = BigDecimal.valueOf(movingSeconds)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal km = totalDistance.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        BigDecimal pace = km.compareTo(BigDecimal.ZERO) > 0
                ? minutes.divide(km, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int calories = km.multiply(BigDecimal.valueOf(60)).intValue();
        s.setCalories(calories);
        s.setActualDistance(totalDistance);
        s.setAveragePace(pace);

        sessionRepo.save(s);

        //완료율 계산(저장하진 않고)
        BigDecimal completionRate = calculateCompletionRate(s.getRouteId(), totalDistance);

        return new CompleteSessionRes(
                s.getSessionId(),
                s.getStartTime(),
                s.getEndTime(),
                totalDistance,
                pace,
                calories,
                completionRate
        );
    }

//완료율 계산 메서드
    private BigDecimal calculateCompletionRate(Integer routeId, BigDecimal actualDistance) {
        if (routeId == null) return BigDecimal.ZERO;

        return routeRepo.findById(routeId)
                .map(route -> {
                    BigDecimal targetDistance = route.getTotalDistance();
                    if (targetDistance == null || targetDistance.compareTo(BigDecimal.ZERO) == 0) {
                        return BigDecimal.ZERO;
                    }
                    return actualDistance
                            .divide(targetDistance, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                })
                .orElse(BigDecimal.ZERO);
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
    public SessionDetailRes getDetail(Integer sessionId) {
        RunningSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        return new SessionDetailRes(s.getSessionId(), s.getStartTime(), s.getEndTime(),
                s.getActualDistance(), s.getAveragePace(), s.getCalories(), s.getStatus());
    }

    // 7️⃣ 러닝 목록 조회 (완료된 세션만, 최신순)
    @Transactional(readOnly = true)
    public Page<SessionItemRes> getList(Integer userId, int page, int size) {
        return sessionRepo.findByUserIdAndStatusOrderByStartTimeDesc(userId, "completed", PageRequest.of(page, size))
                .map(s -> new SessionItemRes(
                        s.getSessionId(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getActualDistance(),
                        s.getAveragePace(),
                        s.getCalories()
                ));
    }

    // 7️⃣ 사용자 통계 요약 (완료 기준: 총 횟수 / 총 거리 / 평균 페이스)
    @Transactional(readOnly = true)
    public StatisticsRes getStatistics(Integer userId) {
        List<Object[]> results = sessionRepo.getStatisticsSummary(userId);

        if (results == null || results.isEmpty()) {
            return new StatisticsRes(0, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Object[] row = results.get(0);
        Number count = (Number) row[0];
        Number totalDistance = (Number) row[1];
        Number avgPace = (Number) row[2];

        return new StatisticsRes(
                count.intValue(),
                BigDecimal.valueOf(totalDistance.doubleValue()),
                BigDecimal.valueOf(avgPace.doubleValue())
        );
    }


    // 6️⃣ GPS 포인트 목록 조회 (지도 시각화용)
    @Transactional(readOnly = true)
    public List<GpsPointRes> getPoints(Integer sessionId) {
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

    @Transactional(readOnly = true)
    public AnalysisRes analyze(Integer sessionId) {
        RunningSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        var points = gpsRepo.findBySessionIdOrderByRecordedAtAsc(sessionId);

        if (points.isEmpty()) {
            return new AnalysisRes(s.getSessionId(), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, 0, List.of());
        }

        // 거리, 속도, 페이스 계산
        BigDecimal totalDistance = BigDecimal.ZERO;
        double maxSpeed = 0;
        double totalSpeed = 0;
        int totalCalories = 0;
        List<AnalysisRes.KmSplit> splits = new ArrayList<>();

        BigDecimal segmentDistance = BigDecimal.ZERO;
        Instant segmentStart = points.get(0).getRecordedAt();

        for (int i = 1; i < points.size(); i++) {
            double dist = distance(points.get(i - 1).getLocation(), points.get(i).getLocation());
            totalDistance = totalDistance.add(BigDecimal.valueOf(dist));
            segmentDistance = segmentDistance.add(BigDecimal.valueOf(dist));

            double speed = Optional.ofNullable(points.get(i).getSpeed())
                    .map(BigDecimal::doubleValue)
                    .orElse(0.0);

            totalSpeed += speed;
            maxSpeed = Math.max(maxSpeed, speed);

            // 1km 마다 split 기록
            if (segmentDistance.doubleValue() >= 1000.0 || i == points.size() - 1) {
                Instant segmentEnd = points.get(i).getRecordedAt();
                double seconds = Duration.between(segmentStart, segmentEnd).getSeconds();
                BigDecimal minutes = BigDecimal.valueOf(seconds / 60.0);
                BigDecimal km = segmentDistance.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
                BigDecimal pace = km.compareTo(BigDecimal.ZERO) > 0 ?
                        minutes.divide(km, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                int calories = km.multiply(BigDecimal.valueOf(60)).intValue();
                totalCalories += calories;

                splits.add(new AnalysisRes.KmSplit(
                        splits.size() + 1,
                        segmentDistance,
                        pace,
                        calories
                ));

                segmentDistance = BigDecimal.ZERO;
                segmentStart = segmentEnd;
            }
        }

        BigDecimal avgSpeed = BigDecimal.valueOf(totalSpeed / points.size());
        BigDecimal avgPace = s.getAveragePace() != null ? s.getAveragePace() : BigDecimal.ZERO;

        return new AnalysisRes(
                s.getSessionId(),
                totalDistance,
                avgPace,
                BigDecimal.valueOf(maxSpeed),
                avgSpeed,
                totalCalories,
                splits
        );
    }

}
