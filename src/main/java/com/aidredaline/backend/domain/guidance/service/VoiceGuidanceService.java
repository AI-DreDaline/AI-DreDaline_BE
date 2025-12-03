package com.aidredaline.backend.domain.guidance.service;

import com.aidredaline.backend.domain.runningsession.geo.GeoFactory;
import com.aidredaline.backend.domain.guidance.dto.GuidancePointDto;
import com.aidredaline.backend.domain.guidance.entity.GuidanceTemplate;
import com.aidredaline.backend.domain.guidance.entity.TurnPoint;
import com.aidredaline.backend.domain.guidance.repository.GuidanceTemplateRepository;
import com.aidredaline.backend.domain.guidance.repository.TurnPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceGuidanceService {

    private final GuidanceTemplateRepository templateRepo;
    private final TurnPointRepository turnPointRepo;
    private final GeoFactory geoFactory;

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    /**
     * Flask에서 턴바이턴 데이터를 받아서 DB에 저장
     * Flask → Backend 호출 시 사용
     */
    @Transactional
    public void saveTurnPoints(Integer routeId, List<FlaskTurnPointDto> turnPoints) {
        log.info("턴바이턴 데이터 저장 시작: routeId={}, points={}", routeId, turnPoints.size());

        // 1. (재생성이면)기존 데이터 삭제
        turnPointRepo.deleteByRouteId(routeId);

        // 2. Flask DTO → Entity 변환
        List<TurnPoint> entities = turnPoints.stream()
                .map(dto -> convertToEntity(routeId, dto))
                .collect(Collectors.toList());

        // 3. DB 저장
        turnPointRepo.saveAll(entities);

        log.info("턴바이턴 데이터 저장 완료: routeId={}, saved={}", routeId, entities.size());
    }

    /**
     * 러닝 시작 시 호출: 턴바이턴 데이터 조회 및 TTS URL 매핑
     * Frontend ← Backend 응답 시 사용
     */
    @Transactional(readOnly = true)
    public List<GuidancePointDto> getGuidancePoints(Integer routeId) {
        log.info("안내 데이터 조회: routeId={}", routeId);

        List<TurnPoint> turnPoints = turnPointRepo.findByRouteIdOrderBySequenceAsc(routeId);

        if (turnPoints.isEmpty()) {
            log.warn("턴바이턴 데이터 없음: routeId={}", routeId);
            return List.of();
        }

        List<GuidancePointDto> guidancePoints = turnPoints.stream()
                .map(this::mapToGuidanceDto)
                .collect(Collectors.toList());

        log.info("안내 데이터 반환: routeId={}, points={}", routeId, guidancePoints.size());
        return guidancePoints;
    }

    /**
     * guidance_id → TTS URL 변환 (캐싱)
     */
    @Cacheable(value = "ttsUrls", key = "#guidanceId")
    public String getTtsUrl(String guidanceId) {
        return templateRepo.findByGuidanceId(guidanceId)
                .map(template -> serverUrl + "/tts/" + template.getFilePath())
                .orElseGet(() -> {
                    log.warn("⚠️ guidance_id not found: {}, using default", guidanceId);
                    return serverUrl + "/tts/default.mp3";
                });
    }

    /**
     * guidance_id → 텍스트 조회 (캐싱)
     */
    @Cacheable(value = "guidanceTexts", key = "#guidanceId")
    public String getGuidanceText(String guidanceId) {
        return templateRepo.findByGuidanceId(guidanceId)
                .map(GuidanceTemplate::getText)
                .orElse("");
    }

    /**
     * Flask DTO → TurnPoint Entity
     */
    // VoiceGuidanceService.java - convertToEntity() 메서드
    private TurnPoint convertToEntity(Integer routeId, FlaskTurnPointDto dto) {
        return TurnPoint.builder()
                .routeId(routeId)
                .sequence(dto.sequence())
                .lat(dto.lat())
                .lng(dto.lng())
                .location(geoFactory.point(dto.lat(), dto.lng()))  //flask에서 lat, lng 순서로 받음 주의할 것
                .direction(dto.direction())
                .angle(dto.angle())
                .distanceFromStart(BigDecimal.valueOf(dto.distanceFromStart()))
                .distanceToNext(BigDecimal.valueOf(dto.distanceToNext()))
                .guidanceId(dto.guidanceId())
                .triggerDistance(dto.triggerDistance())
                .build();
    }

    /**
     * TurnPoint Entity → Frontend DTO 변환
     */
    private GuidancePointDto mapToGuidanceDto(TurnPoint turn) {
        return GuidancePointDto.builder()
                .sequence(turn.getSequence())
                .lat(turn.getLat())
                .lng(turn.getLng())
                .direction(turn.getDirection())
                .angle(turn.getAngle())
                .distanceFromStart(turn.getDistanceFromStart())
                .distanceToNext(turn.getDistanceToNext())
                .guidanceId(turn.getGuidanceId())
                .guidanceText(getGuidanceText(turn.getGuidanceId()))  // DB 조회
                .ttsUrl(getTtsUrl(turn.getGuidanceId()))              // DB 조회 + URL 생성
                .triggerDistance(turn.getTriggerDistance())
                .build();
    }

    /**
     * Flask DTO (내부 클래스)
     */
    public record FlaskTurnPointDto(
            Integer sequence,
            String type,  // "turn", "straight" 등 (사용 안 할 수도 있음)
            Double lat,
            Double lng,
            String direction,
            Double angle,
            Double distanceFromStart,
            Double distanceToNext,
            String guidanceId,
            Double triggerDistance
    ) {}
}
