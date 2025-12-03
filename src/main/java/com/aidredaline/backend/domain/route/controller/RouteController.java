package com.aidredaline.backend.domain.route.controller;

import com.aidredaline.backend.common.dto.ApiResponse;
import com.aidredaline.backend.domain.route.dto.RouteGenerateRequest;
import com.aidredaline.backend.domain.route.dto.RouteGenerateResponse;
import com.aidredaline.backend.domain.route.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * - 경로 생성/조회 API 제공
 * Base: /api/routes
 */
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Route", description = "경로 생성 및 조회 API")
public class RouteController {

    private final RouteService routeService;

    /**
     *  POST /api/routes/generate  : 경로 생성
     * 1. 사용자가 템플릿 선택 (예: Star)
     * 2. 시작 지점과 목표 거리 입력
     * 3. Flask AI 서버(또는 Mock)가 경로 생성
     * 4. 생성된 경로를 DB에 저장
     * 5. 경로 정보 반환 (좌표 배열 포함)
     *
     * @param request 경로 생성 요청
     * @return 생성된 경로 정보
     */
    @PostMapping("/generate")
    @Operation(
            summary = "경로 생성",
            description = """
            템플릿을 기반으로 러닝 경로를 생성합니다.
            
            **요청 파라미터:**
            - userId: 사용자 ID (MVP: 1 고정)
            - templateId: 템플릿 ID (1=Heart, 2=Star, 3=Circle, ...)
            - startPoint: 시작 지점 (위도, 경도)
            - targetDistance: 목표 거리 (km)
            
            **처리 과정:**
            1. 템플릿 조회
            2. Flask AI 서버 호출 (개발 환경에서는 Mock 사용)
            3. 도로를 따라 템플릿 모양의 경로 생성
            4. DB 저장
            
            **응답:**
            - routeId: 생성된 경로 ID
            - routePath: 경로 좌표 배열 (지도에 그리기 위한 데이터)
            - totalDistance: 실제 생성된 거리 (약간의 오차 있음)
            - expectedDuration: 예상 소요 시간 (mock은 6:00페이스로 잡았음)
            - similarityScore: 템플릿 유사도 (0.0 ~ 1.0: 랜덤값으로 나옴)
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "경로 생성 성공",
                    content = @Content(schema = @Schema(implementation = RouteGenerateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "템플릿을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "Flask 서버 통신 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<RouteGenerateResponse>> generateRoute(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "경로 생성 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RouteGenerateRequest.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                        {
                          "userId": 1,
                          "templateId": 1,
                          "startPoint": {
                            "latitude": 33.4996,
                            "longitude": 126.5312
                          },
                          "targetDistance": 5.0
                        }
                        """
                            )
                    )
            )
            RouteGenerateRequest request
    ) {
        log.info("POST /api/routes/generate - 경로 생성 요청");
        log.info("userId: {}", request.getUserId());
        log.info("templateId: {}", request.getTemplateId());
        log.info("startPoint: ({}, {})",
                request.getStartPoint().getLatitude(),
                request.getStartPoint().getLongitude());
        log.info("   targetDistance: {}km", request.getTargetDistance());

        RouteGenerateResponse response = routeService.generateRoute(request);

        log.info("경로 생성 완료");
        log.info("routeId: {}", response.getRouteId());
        log.info("userId: {}", request.getUserId());
        log.info("totalDistance: {}km", response.getTotalDistance());
        log.info("노드 개수: {}", response.getRoutePath().size());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * GET /api/routes/{routeId}  : 경로 조회
     * @param routeId 경로 ID
     * @return 경로 정보
     */
    @GetMapping("/{routeId}")
    @Operation(
            summary = "경로 조회",
            description = """
            경로 ID로 생성된 경로 정보를 조회합니다.
            
            **응답:**
            - 경로 생성 시와 동일한 정보 반환
            - routePath: 지도에 그릴 좌표 배열
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RouteGenerateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "경로를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<RouteGenerateResponse>> getRoute(
            @Parameter(
                    description = "경로 ID",
                    example = "1",
                    required = true
            )
            @PathVariable Integer routeId
    ) {
        log.info("GET /api/routes/{} - 경로 조회", routeId);
        RouteGenerateResponse response = routeService.getRoute(routeId);

        log.info("경로 조회 완료 - routeId: {}", routeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/routes/{routeId}/save : 경로 저장
     * 추천 경로로 달리기_최종 화면에서 "안내 시작" 버튼 누르면 생성된 경로가 저장되도록
     * is_saved = false → true로 변경
     */
    @PostMapping("/{routeId}/save")
    @Operation(
            summary = "경로 저장",
            description = """
            생성된 경로를 저장합니다.
            
            **사용 시나리오:**
            1. 사용자가 여러 경로를 생성해봄 (POST /api/routes/generate)
            2. 마음에 드는 경로 발견
            3. 저장 버튼 클릭 → 이 API 호출
            4. is_saved = true로 변경되어 "내 경로" 목록에 표시됨
            
            **권한:**
            - 본인의 경로만 저장 가능
            - 다른 사용자의 경로 저장 시도 시 400 에러
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "경로 저장 성공",
                    content = @Content(schema = @Schema(implementation = RouteGenerateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "권한 없음 (다른 사용자의 경로)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "경로를 찾을 수 없음"
            )
    })
    public ResponseEntity<ApiResponse<RouteGenerateResponse>> saveRoute(
            @Parameter(description = "경로 ID", example = "1", required = true)
            @PathVariable Integer routeId,

            @Parameter(description = "사용자 ID (MVP: 요청 파라미터)", example = "1", required = true)
            @RequestParam Integer userId
    ) {
        log.info("POST /api/routes/{}/save - userId: {}", routeId, userId);

        RouteGenerateResponse response = routeService.saveRoute(routeId, userId);

        log.info("경로 저장 완료 - routeId: {}, userId: {}", routeId, userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/routes/saved : 나의 경로 목록 조회
     * (is_saved = true) 저장된 경로 목록 조회
     */
    @GetMapping("/saved")
    @Operation(
            summary = "저장된 경로 목록",
            description = """
            사용자가 저장한 경로 목록을 조회합니다.
            
            **필터링:**
            - is_saved = true인 경로만 반환
            - 해당 사용자의 경로만 반환
            - 최신순 정렬 (created_at DESC)
            
            **사용 시나리오:**
            - "내 경로" 화면에서 사용
            - 저장된 경로 중에서 선택해서 러닝 시작
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RouteGenerateResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<RouteGenerateResponse>>> getSavedRoutes(
            @Parameter(description = "사용자 ID", example = "1", required = true)
            @RequestParam Integer userId
    ) {
        log.info("GET /api/routes/saved - userId: {}", userId);
        List<RouteGenerateResponse> routes = routeService.getSavedRoutes(userId);

        log.info("저장된 경로 조회 완료 - userId: {}, 개수: {}", userId, routes.size());
        return ResponseEntity.ok(ApiResponse.success(routes));
    }


    /**
     * DELETE /api/routes/{routeId}  : 경로 삭제
     */
    @DeleteMapping("/{routeId}")
    @Operation(
            summary = "경로 삭제",
            description = """
            생성된 경로를 삭제합니다.
            
            **사용 시나리오:**
            - 마음에 안 드는 임시 경로 삭제하는 경우
            - 저장했던 경로 삭제하는 경우 
            
            **권한:**
            - 본인의 경로만 삭제 가능
            - 다른 사용자의 경로 삭제 시도 시 400 에러
            
            **주의:**
            - 삭제된 경로는 복구 불가능
            - 해당 경로로 러닝한 세션 데이터는 유지됨
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "권한 없음 (다른 사용자의 경로)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "경로를 찾을 수 없음"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteRoute(
            @Parameter(description = "경로 ID", example = "1", required = true)
            @PathVariable Integer routeId,

            @Parameter(description = "사용자 ID (MVP: 요청 파라미터)", example = "1", required = true)
            @RequestParam Integer userId
    ) {
        log.info("DELETE /api/routes/{} - userId: {}", routeId, userId);
        routeService.deleteRoute(routeId, userId);

        log.info("경로 삭제 완료 - routeId: {}, userId: {}", routeId, userId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}