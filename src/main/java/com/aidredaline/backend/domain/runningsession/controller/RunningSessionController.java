package com.aidredaline.backend.domain.runningsession.controller;

import com.aidredaline.backend.domain.runningsession.dto.*;
import com.aidredaline.backend.domain.runningsession.service.RunningSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/running-sessions")
@RequiredArgsConstructor
public class RunningSessionController {

    private final RunningSessionService service;

    // 1️⃣ 러닝 세션 시작
    @PostMapping("/start")
    public StartSessionRes start(@RequestBody StartSessionReq req) {
        return service.start(req);
    }

    // 2️⃣ GPS 트래킹 데이터 저장
    @PostMapping("/{sessionId}/tracking")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void track(@PathVariable Integer sessionId, @RequestBody TrackReq req) {
        service.track(sessionId, req);
    }

    // 3️⃣ 러닝 일시정지
    @PatchMapping("/{sessionId}/pause")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pause(@PathVariable Integer sessionId) {
        service.pause(sessionId);
    }

    // 3️⃣ 러닝 재개
    @PatchMapping("/{sessionId}/resume")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resume(@PathVariable Integer sessionId) {
        service.resume(sessionId);
    }

    // 4️⃣ 러닝 완료 및 분석
    @PostMapping("/{sessionId}/complete")
    public CompleteSessionRes complete(@PathVariable Integer sessionId) {
        return service.complete(sessionId);
    }

    // 5️⃣ 러닝 상세 조회
    @GetMapping("/{sessionId}")
    public SessionDetailRes getDetail(@PathVariable Integer sessionId) {
        return service.getDetail(sessionId);
    }

    // 6️⃣ GPS 포인트 목록 조회 (지도 시각화용)
    @GetMapping("/{sessionId}/points")
    public List<GpsPointRes> getPoints(@PathVariable Integer sessionId) {
        return service.getPoints(sessionId);
    }

    // 7️⃣ 러닝 목록 조회 (완료된 세션만, 최신순, 페이지네이션)
    @GetMapping
    public Page<SessionItemRes> getList(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getList(userId, page, size);
    }

    // 7️⃣ 사용자 통계 요약 (완료 기준: 총 횟수 / 총 거리 / 평균 페이스)
    @GetMapping("/statistics/{userId}")
    public StatisticsRes getStatistics(@PathVariable Integer userId) {
        return service.getStatistics(userId);
    }

    // 8️⃣ 러닝 세션 상세 분석
    @GetMapping("/{sessionId}/analysis")
    public AnalysisRes analyze(@PathVariable Integer sessionId) {
        return service.analyze(sessionId);
    }


}



