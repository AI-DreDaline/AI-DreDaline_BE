package com.aidredaline.backend.external.flask;

import com.aidredaline.backend.external.flask.dto.FlaskRouteRequest;
import com.aidredaline.backend.external.flask.dto.FlaskRouteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Flask Mock 클라이언트 (BE-개발용)
 * 활성화:
 * - @Profile("!prod"): 개발/테스트 환경에서만 사용
 */
@Component
@Profile("!prod")
@Slf4j
public class MockFlaskClient implements FlaskClient {

    private final Random random = new Random();

    @Override
    public FlaskRouteResponse generateRoute(FlaskRouteRequest request) {
        log.info(" MockFlaskClient - 경로 생성 시작");
        log.info(" 템플릿: {}", request.getTemplateName());
        log.info(" 시작점: ({}, {})",
                request.getStartPoint().getLat(),
                request.getStartPoint().getLng());
        log.info("   목표거리: {}km", request.getTargetKm());

        // Mock 경로 생성
        List<List<Double>> finalPoints = generateMockPath(
                request.getStartPoint().getLng(),
                request.getStartPoint().getLat(),
                request.getTargetKm()
        );

        // 실제 거리 계산 (약간의 오차 추가)
        double actualDistanceM = request.getTargetKm() * 1000 * (0.95 + random.nextDouble() * 0.1);

        // Mock 응답 생성 (실제 Flask 응답 구조와 동일)
        FlaskRouteResponse response = FlaskRouteResponse.builder()
                .ok(true)
                .data(FlaskRouteResponse.FlaskDataDto.builder()
                        .geojson(createMockGeoJson(finalPoints, request.getTemplateName()))
                        .metrics(FlaskRouteResponse.FlaskMetricsDto.builder()
                                .targetKm(request.getTargetKm())
                                .routeLengthM(actualDistanceM)
                                .nodes(finalPoints.size())
                                .scaleMPerUnit(50.0)
                                .build())
                        .templatePoints(finalPoints)  // Mock에서는 동일하게
                        .routePoints(finalPoints)     // Mock에서는 동일하게
                        .finalPoints(finalPoints)     // 실제 사용할 경로
                        .build())
                .build();

        log.info("!!!!!!!!!!!!!!!!!MockFlaskClient - 경로 생성 완료!!!!!!!!!!!!!!!");
        log.info(" 노드: {}개", finalPoints.size());
        log.info(" 실제 거리: {:.2f}km", actualDistanceM / 1000);

        return response;
    }

    /**
     * Mock 경로 생성
     *
     * 알고리즘:
     * 1. 시작점에서 출발
     * 2. 원형으로 이동하며 좌표 생성
     * 3. 약간의 랜덤 노이즈 추가 (자연스러움)
     * 4. 시작점으로 복귀
     *
     * @param startLng 시작 경도
     * @param startLat 시작 위도
     * @param targetKm 목표 거리 (km)
     * @return 경로 좌표 배열 [[lng, lat], [lng, lat], ...]
     */
    private List<List<Double>> generateMockPath(Double startLng, Double startLat, Double targetKm) {
        List<List<Double>> path = new ArrayList<>();

        // 1km당 20개 포인트
        int numPoints = (int) (targetKm * 20);

        // 원형 경로를 만들기 위한 설정
        double angleStep = (2 * Math.PI) / numPoints;
        // 반지름 (km → 도 변환, 대략적인 값)
        double radius = (targetKm / (2 * Math.PI)) * 0.009;  // 적절한 반지름

        // 1. 시작점
        path.add(List.of(startLng, startLat));

        // 2. 원형으로 이동
        for (int i = 1; i < numPoints; i++) {
            double angle = i * angleStep;

            // 원형 좌표 계산 + 랜덤 노이즈
            double noise = (random.nextDouble() - 0.5) * 0.0005;
            double lng = startLng + radius * Math.cos(angle) + noise;
            double lat = startLat + radius * Math.sin(angle) + noise;

            path.add(List.of(lng, lat));
        }

        // 3. 시작점으로 복귀
        path.add(List.of(startLng, startLat));

        return path;
    }

    /**
     * Mock GeoJSON 생성
     * @param coordinates 좌표 배열
     * @param templateName 템플릿 이름
     * @return GeoJSON Map
     */
    private HashMap<String, Object> createMockGeoJson(List<List<Double>> coordinates, String templateName) {
        HashMap<String, Object> geojson = new HashMap<>();
        geojson.put("type", "FeatureCollection");

        List<HashMap<String, Object>> features = new ArrayList<>();
        HashMap<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Mock Route - " + templateName);
        properties.put("matched", true);
        feature.put("properties", properties);

        HashMap<String, Object> geometry = new HashMap<>();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);
        feature.put("geometry", geometry);

        features.add(feature);
        geojson.put("features", features);

        return geojson;
    }
}