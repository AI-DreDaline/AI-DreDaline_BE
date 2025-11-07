package com.aidredaline.backend.domain.route.service;

import com.aidredaline.backend.common.exception.RouteNotFoundException;
import com.aidredaline.backend.common.exception.TemplateNotFoundException;
import com.aidredaline.backend.domain.route.dto.RouteGenerateRequest;
import com.aidredaline.backend.domain.route.dto.RouteGenerateResponse;
import com.aidredaline.backend.domain.route.entity.GeneratedRoute;
import com.aidredaline.backend.domain.route.repository.GeneratedRouteRepository;
import com.aidredaline.backend.domain.template.entity.ShapeTemplate;
import com.aidredaline.backend.domain.template.repository.ShapeTemplateRepository;
import com.aidredaline.backend.external.flask.FlaskClient;
import com.aidredaline.backend.external.flask.dto.FlaskRouteRequest;
import com.aidredaline.backend.external.flask.dto.FlaskRouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 경로 생성 비즈니스 로직 처리
 * Flask AI 서버 연동 (Mock 또는 Real)
 * 생성된 경로 DB 저장
 *
 * 1. 템플릿 조회
 * 2. Flask 클라이언트 호출
 * 3. 응답 데이터 → Entity 변환
 * 4. DB 저장
 * 5. Entity → DTO 변환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RouteService {

    private final GeneratedRouteRepository routeRepository;
    private final ShapeTemplateRepository templateRepository;
    private final FlaskClient flaskClient;  // Mock 또는 Real이 자동 주입됨!

    // PostGIS용 GeometryFactory (SRID 4326 = WGS84)
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * [경로 생성 플로우]
     * 1. 템플릿 존재 여부 확인
     * 2. Flask 요청 DTO 생성
     * 3. Flask 클라이언트 호출 (Mock/Real)
     * 4. Flask 응답 → Entity 변환
     * 5. DB 저장
     * 6. Entity → DTO 변환 후 반환
     *
     * @param request 경로 생성 요청
     * @return 생성된 경로 정보
     */
    @Transactional
    public RouteGenerateResponse generateRoute(RouteGenerateRequest request) {
        log.info("2경로 생성 시작");
        log.info("   templateId: {}", request.getTemplateId());
        log.info("   startPoint: ({}, {})",
                request.getStartPoint().getLatitude(),
                request.getStartPoint().getLongitude());
        log.info("   targetDistance: {}km", request.getTargetDistance());

        // 1. 템플릿 조회
        ShapeTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new TemplateNotFoundException(
                        "템플릿을 찾을 수 없습니다. ID: " + request.getTemplateId()));

        log.info("   템플릿 조회 완료: {}", template.getName());

        // 2. Flask 요청 DTO 생성
        FlaskRouteRequest flaskRequest = FlaskRouteRequest.builder()
                .startPoint(FlaskRouteRequest.FlaskPointDto.builder()
                        .lat(request.getStartPoint().getLatitude())
                        .lng(request.getStartPoint().getLongitude())
                        .build())
                .targetKm(request.getTargetDistance())
                .templateName(template.getName().toLowerCase() + ".svg")  // "Star" → "star.svg"
                .options(FlaskRouteRequest.FlaskOptionsDto.builder()
                        .mapMatch(true)
                        .rotationDeg(0.0)
                        .build())
                .build();

        // 3. Flask 클라이언트 호출 (Mock 또는 Real)
        log.info("Flask 클라이언트 호출 중...");
        FlaskRouteResponse flaskResponse = flaskClient.generateRoute(flaskRequest);

        if (!Boolean.TRUE.equals(flaskResponse.getOk())) {
            log.error("Flask 응답 실패");
            throw new RuntimeException("Flask 경로 생성 실패");
        }

        log.info("Flask 응답 성공");

        // 4. Flask 응답 → Entity 변환
        GeneratedRoute route = convertFlaskResponseToEntity(
                flaskResponse,
                request,
                template
        );

        // 5. DB 저장
        GeneratedRoute savedRoute = routeRepository.save(route);
        log.info("DB 저장 완료 - routeId: {}", savedRoute.getRouteId());

        // 6. Entity → DTO 변환
        RouteGenerateResponse response = RouteGenerateResponse.from(
                savedRoute,
                template.getName()
        );

        log.info("경로 생성 완료");
        log.info("routeId: {}", response.getRouteId());
        log.info("totalDistance: {}km", response.getTotalDistance());
        log.info("노드 개수: {}", response.getRoutePath().size());

        return response;
    }

    /**
     * 경로 조회
     * @param routeId 경로 ID
     * @return 경로 정보
     */
    public RouteGenerateResponse getRoute(Integer routeId) {
        log.info("경로 조회 - routeId: {}", routeId);

        GeneratedRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(
                        "경로를 찾을 수 없습니다. ID: " + routeId));

        // 템플릿 이름 조회
        String templateName = templateRepository.findById(route.getTemplateId())
                .map(ShapeTemplate::getName)
                .orElse("Unknown");

        return RouteGenerateResponse.from(route, templateName);
    }

    /**
     * Flask 응답 → Entity 변환
     * @param flaskResponse Flask 응답
     * @param request 원래 요청
     * @param template 템플릿
     * @return GeneratedRoute Entity
     */
    private GeneratedRoute convertFlaskResponseToEntity(
            FlaskRouteResponse flaskResponse,
            RouteGenerateRequest request,
            ShapeTemplate template
    ) {
        FlaskRouteResponse.FlaskDataDto data = flaskResponse.getData();
        FlaskRouteResponse.FlaskMetricsDto metrics = data.getMetrics();

        // 시작점 생성 (PostGIS Point)
        Point startPoint = createPoint(
                request.getStartPoint().getLongitude(),
                request.getStartPoint().getLatitude()
        );

        // 경로 생성 (PostGIS LineString)
        LineString routePath = createLineString(data.getFinalPoints());

        //원본 템플릿 형태 (있다면)
        LineString originalShape = null;
        if (data.getTemplatePoints() != null && !data.getTemplatePoints().isEmpty()) {
            originalShape = createLineString(data.getTemplatePoints());
        }

        // 거리 계산 (미터 → km)
        Double totalDistanceKm = metrics.getRouteLengthM() / 1000.0;

        // 예상 소요 시간 계산 (6:00 페이스로 계산했음)
        Integer expectedDuration = (int) (totalDistanceKm * 6 * 60);

        // 유사도 점수 (Mock에서는 랜덤, Real에서는 실제 계산값)
        Double similarityScore = calculateSimilarityScore(routePath, originalShape);

        return GeneratedRoute.builder()
                .userId(request.getUserId())
                .templateId(template.getTemplateId())
                .startPoint(startPoint)
                .routePath(routePath)
                .originalShape(originalShape)
                .totalDistance(totalDistanceKm)
                .expectedDuration(expectedDuration)
                .similarityScore(similarityScore)
                .build();
    }

    /**
     * PostGIS Point 생성
     * @param longitude 경도
     * @param latitude 위도
     * @return PostGIS Point
     */
    private Point createPoint(Double longitude, Double latitude) {
        Coordinate coordinate = new Coordinate(longitude, latitude);
        return geometryFactory.createPoint(coordinate);
    }

    /**
     * PostGIS LineString 생성
     * @param points [[경도, 위도], [경도, 위도], ...]
     * @return PostGIS LineString
     */
    private LineString createLineString(List<List<Double>> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }

        Coordinate[] coordinates = points.stream()
                .map(point -> new Coordinate(point.get(0), point.get(1)))  // [lng, lat]
                .toArray(Coordinate[]::new);

        return geometryFactory.createLineString(coordinates);
    }

    /**
     * 유사도 점수 계산
     * - Mock: 0.85 ~ 0.95 랜덤
     * - Real: Flask에서 계산된 값 사용 (향후)
     *
     * @param routePath 생성된 경로
     * @param originalShape 원본 템플릿 형태
     * @return 유사도 점수 (0.0 ~ 1.0)
     */
    private Double calculateSimilarityScore(LineString routePath, LineString originalShape) {
        // MVP: 간단하게 랜덤 값 (0.85 ~ 0.95)
        return 0.85 + (Math.random() * 0.10);

        // TODO: 실제 유사도 계산 알고리즘
        // - Hausdorff Distance
        // - Frechet Distance
        // - Dynamic Time Warping
    }

    /**
     * 사용자가 생성된 경로를 마음에 들어해서 저장하는 기능
     * is_saved = false → true로 변경
     * @param routeId 경로 ID
     * @param userId 사용자 ID
     * @return 저장된 경로 정보
     */
    @Transactional
    public RouteGenerateResponse saveRoute(Integer routeId, Integer userId) {
        log.info("경로 저장 - routeId: {}, userId: {}", routeId, userId);

        // 1. 경로 조회
        GeneratedRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        // 2. 본인의 경로인지 확인
        if (!route.getUserId().equals(userId)) {
            log.error("권한 없음 - 경로 소유자: {}, 요청자: {}", route.getUserId(), userId);
            throw new IllegalArgumentException("본인의 경로만 저장할 수 있습니다.");
        }

        // 3. 이미 저장된 경로인지 확인
        if (Boolean.TRUE.equals(route.getIsSaved())) {
            log.info("이미 저장된 경로입니다 - routeId: {}", routeId);
        } else {
            // 4. 저장 처리 (is_saved = true)
            route.save();
            routeRepository.save(route);
            log.info("경로 저장 완료 - routeId: {}", routeId);
        }

        // 5. 템플릿 이름 조회
        String templateName = templateRepository.findById(route.getTemplateId())
                .map(ShapeTemplate::getName)
                .orElse("Unknown");

        return RouteGenerateResponse.from(route, templateName);
    }

    /**
     * 사용자가 저장한 경로들만 조회 (is_saved = true)
     * 최신순으로 정렬
     * @param userId 사용자 ID
     * @return 저장된 경로 목록
     */
    public List<RouteGenerateResponse> getSavedRoutes(Integer userId) {
        log.info("저장된 경로 목록 조회 - userId: {}", userId);

        // 1. is_saved = true인 경로만 조회
        List<GeneratedRoute> routes = routeRepository
                .findByUserIdAndIsSavedOrderByCreatedAtDesc(userId, true);

        log.info("저장된 경로 개수: {}", routes.size());

        // 2. Entity → DTO 변환
        return routes.stream()
                .map(route -> {
                    String templateName = templateRepository.findById(route.getTemplateId())
                            .map(ShapeTemplate::getName)
                            .orElse("Unknown");
                    return RouteGenerateResponse.from(route, templateName);
                })
                .toList();
    }

    /**
     * 사용자가 생성한 경로를 삭제
     * (임시 경로나 저장된 경로 모두 삭제 가능)
     * @param routeId 경로 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteRoute(Integer routeId, Integer userId) {
        log.info("경로를 삭제합니다. - routeId: {}, userId: {}", routeId, userId);

        // 1. 경로 조회
        GeneratedRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        // 2. 본인의 경로인지 확인
        if (!route.getUserId().equals(userId)) {
            log.error("권한 없음 - 경로 소유자: {}, 요청자: {}", route.getUserId(), userId);
            throw new IllegalArgumentException("본인의 경로만 삭제할 수 있습니다.");
        }

        // 3. 삭제
        routeRepository.delete(route);
        log.info("경로 삭제 완료 - routeId: {}", routeId);
    }
}
