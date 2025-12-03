package com.aidredaline.backend.domain.guidance.controller;

import com.aidredaline.backend.domain.guidance.dto.GuidancePointDto;
import com.aidredaline.backend.domain.guidance.service.VoiceGuidanceService;
import com.aidredaline.backend.domain.guidance.service.VoiceGuidanceService.FlaskTurnPointDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/guidance")
@RequiredArgsConstructor
public class GuidanceController {
    //TODO Flask에서 턴바이턴 데이터를 받을 API와, 나중에 테스트용 API

    private final VoiceGuidanceService guidanceService;

    /**
     * Flask → Backend: 턴바이턴 데이터 수신 및 저장
     * POST /api/guidance/turn-points
     * 예시 :
     * {
     *   "routeId": 123,
     *   "turnPoints": [
     *     {
     *       "sequence": 1,
     *       "type": "turn",
     *       "lat": 33.499074,
     *       "lng": 126.531599,
     *       "direction": "left",
     *       "angle": -92.7,
     *       "distanceFromStart": 44.4,
     *       "distanceToNext": 104.4,
     *       "guidanceId": "TURN_LEFT_50",
     *       "triggerDistance": 50
     *     }
     *   ]
     * }
     */
    @PostMapping("/turn-points")
    public ResponseEntity<SaveTurnPointsResponse> saveTurnPoints(
            @RequestBody SaveTurnPointsRequest request) {

        log.info("턴바이턴 데이터 수신: routeId={}, points={}",
                request.routeId(), request.turnPoints().size());
        guidanceService.saveTurnPoints(request.routeId(), request.turnPoints());

        return ResponseEntity.ok(
                new SaveTurnPointsResponse(
                        request.routeId(),
                        request.turnPoints().size(),
                        "저장 완료"
                )
        );
    }

    /**
     * Frontend/테스트용: 특정 경로의 안내 데이터 조회
     * GET /api/guidance/routes/{routeId}
     */
    @GetMapping("/routes/{routeId}")
    public ResponseEntity<GuidanceResponse> getGuidancePoints(
            @PathVariable Integer routeId) {

        log.info("안내 데이터 조회 요청: routeId={}", routeId);

        List<GuidancePointDto> guidancePoints = guidanceService.getGuidancePoints(routeId);
        return ResponseEntity.ok(
                new GuidanceResponse(
                        routeId,
                        guidancePoints.size(),
                        guidancePoints
                )
        );
    }

    /**
     * 테스트용: 특정 guidance_id의 TTS URL 조회
     * GET /api/guidance/tts-url/{guidanceId}
     */
    @GetMapping("/tts-url/{guidanceId}")
    public ResponseEntity<TtsUrlResponse> getTtsUrl(
            @PathVariable String guidanceId) {

        log.info("TTS URL 조회: guidanceId={}", guidanceId);

        String ttsUrl = guidanceService.getTtsUrl(guidanceId);
        String text = guidanceService.getGuidanceText(guidanceId);

        return ResponseEntity.ok(
                new TtsUrlResponse(guidanceId, text, ttsUrl)
        );
    }






    // ===================================
    // Request/Response DTOs
    // ===================================

    /**
     * Flask 요청 DTO
     */
    public record SaveTurnPointsRequest(
            Integer routeId,
            List<FlaskTurnPointDto> turnPoints
    ) {}

    /**
     * Flask 응답 DTO
     */
    public record SaveTurnPointsResponse(
            Integer routeId,
            Integer savedPoints,
            String message
    ) {}

    /**
     * Frontend 응답 DTO
     */
    public record GuidanceResponse(
            Integer routeId,
            Integer totalPoints,
            List<GuidancePointDto> guidancePoints
    ) {}

    /**
     * TTS URL 조회 응답 DTO
     */
    public record TtsUrlResponse(
            String guidanceId,
            String text,
            String ttsUrl
    ) {}
}