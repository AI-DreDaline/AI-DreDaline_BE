package com.aidredaline.backend.domain.runningsession.controller;

import com.aidredaline.backend.domain.runningsession.dto.*;
import com.aidredaline.backend.domain.runningsession.service.RunningSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "러닝 시작",
            description = "새로운 러닝 세션을 시작합니다. 생성된 경로(routeId)를 기반으로 러닝을 시작할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "러닝 세션 시작 성공",
                    content = @Content(schema = @Schema(implementation = StartSessionRes.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (routeId 없음 등)")
    })
    @PostMapping("/start")
    public StartSessionRes start(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "러닝 시작 요청 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StartSessionReq.class))
            )
            @RequestBody StartSessionReq req
    ) {
        return service.start(req);
    }


    // 2️⃣ GPS 트래킹 데이터 저장
    @Operation(
            summary = "GPS 트래킹 데이터 저장",
            description = "실시간 GPS 위치 데이터를 저장합니다. 5초 간격으로 호출을 권장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "GPS 데이터 저장 성공"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "세션이 활성 상태가 아님")
    })
    @PostMapping("/{sessionId}/tracking")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void track(
            @Parameter(description = "세션 ID", required = true, example = "1")
            @PathVariable Integer sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "GPS 트래킹 데이터",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TrackReq.class))
            )
            @RequestBody TrackReq req
    ) {
        service.track(sessionId, req);
    }

    // 3️⃣ 러닝 일시정지
    @Operation(
            summary = "러닝 일시정지",
            description = "진행 중인 러닝을 일시정지합니다. 일시정지 시간은 이동 시간에서 제외됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "일시정지 성공"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "진행 중인 세션이 아님")
    })
    @PatchMapping("/{sessionId}/pause")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pause(
            @Parameter(description = "세션 ID", required = true, example = "1")
            @PathVariable Integer sessionId
    ) {
        service.pause(sessionId);
    }


    // 3️⃣ 러닝 재개
    @Operation(
            summary = "러닝 재개",
            description = "일시정지된 러닝을 재개합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "재개 성공"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "일시정지 상태가 아님")
    })
    @PatchMapping("/{sessionId}/resume")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resume(
            @Parameter(description = "세션 ID", required = true, example = "1")
            @PathVariable Integer sessionId
    ) {
        service.resume(sessionId);
    }


    // 4️⃣ 러닝 완료 및 분석
    @Operation(
            summary = "러닝 완료",
            description = "러닝을 완료하고 총 거리, 페이스, 칼로리, 완료율을 계산합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "러닝 완료 성공",
                    content = @Content(schema = @Schema(implementation = CompleteSessionRes.class))
            ),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "활성 상태의 세션이 아님")
    })
    @PostMapping("/{sessionId}/complete")
    public CompleteSessionRes complete(
            @Parameter(description = "세션 ID", required = true, example = "1")
            @PathVariable Integer sessionId
    ) {
        return service.complete(sessionId);
    }

    // 5️⃣ 러닝 상세 조회
    @Operation(
            summary = "러닝 세션 상세 조회",
            description = "특정 러닝 세션의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SessionDetailRes.class))
            ),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    @GetMapping("/{sessionId}")
    public SessionDetailRes getDetail(
            @Parameter(description = "세션 ID", required = true, example = "1")
            @PathVariable Integer sessionId
    ) {
        return service.getDetail(sessionId);
    }


    // 6️⃣ GPS 포인트 목록 조회 (지도 시각화용)
    @Operation(
            summary = "GPS 포인트 목록 조회",
            description = "지도 시각화를 위한 GPS 트래킹 포인트 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GpsPointRes.class))
            ),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    @GetMapping("/{sessionId}/points")
    public List<GpsPointRes> getPoints(
            @Parameter(description = "세션 ID", required = true, example = "1")
            @PathVariable Integer sessionId
    ) {
        return service.getPoints(sessionId);
    }


    // 7️⃣ 러닝 목록 조회 (완료된 세션만, 최신순, 페이지네이션)
    @Operation(
            summary = "완료된 러닝 목록 조회",
            description = "사용자의 완료된 러닝 세션 목록을 최신순으로 페이지네이션하여 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    public Page<SessionItemRes> getList(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Integer userId,
            @Parameter(description = "페이지 번호 (0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getList(userId, page, size);
    }


    // 7️⃣ 사용자 통계 요약 (완료 기준: 총 횟수 / 총 거리 / 평균 페이스)
    @Operation(
            summary = "사용자 러닝 통계 조회",
            description = "사용자의 전체 러닝 통계를 조회합니다. (총 횟수, 총 거리, 평균 페이스)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = StatisticsRes.class))
            )
    })
    @GetMapping("/statistics/{userId}")
    public StatisticsRes getStatistics(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Integer userId
    ) {
        return service.getStatistics(userId);
    }


    // 8️⃣ 러닝 세션 상세 분석
    @Operation(
            summary = "러닝 세션 상세 분석",
            description = "러닝 세션의 상세 분석 데이터를 조회합니다. (km별 split, 최고 속도, 평균 속도 등)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AnalysisRes.class))
            ),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    @GetMapping("/{sessionId}/analysis")
    public AnalysisRes analyze(
            @Parameter(description = "세션 ID", required = true, example = "1")
            @PathVariable Integer sessionId
    ) {
        return service.analyze(sessionId);
    }


}



