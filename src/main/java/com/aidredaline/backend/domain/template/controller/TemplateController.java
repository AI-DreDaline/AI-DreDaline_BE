package com.aidredaline.backend.domain.template.controller;

import com.aidredaline.backend.common.dto.ApiResponse;
import com.aidredaline.backend.domain.template.dto.TemplateListResponse;
import com.aidredaline.backend.domain.template.dto.TemplateResponse;
import com.aidredaline.backend.domain.template.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//Base: /api/templates

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Template", description = "템플릿 관리 API - 러닝 경로 모양(하트, 별 등) 템플릿 조회")
public class TemplateController {

    private final TemplateService templateService;

    /**
     * 전체 템플릿 목록 조회
     * @return 전체 템플릿 목록
     */
    @GetMapping
    @Operation(
            summary = "전체 템플릿 목록 조회",
            description = """
            사용 가능한 모든 템플릿(별, 고구마, 강아지 등)을 조회합니다.
            
            **응답 정보:**
            - 템플릿 ID, 이름, 카테고리
            - 썸네일 이미지 URL
            - SVG 경로 데이터 (프론트엔드 렌더링용)
            - 평균 거리, 포인트 개수
            
            **사용 예시:**
```
            GET /api/templates
            
            Response:
            {
              "success": true,
              "message": "Success",
              "data": {
                "templates": [
                  {
                    "templateId": 1,
                    "name": "Heart",
                    "category": "romantic",
                    "svgPath": "M 100,30 ...",
                    "avgDistance": 5.0,
                    "pointCount": 100
                  }
                ],
                "totalCount": 5
              }
            }
```
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TemplateListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    public ResponseEntity<ApiResponse<TemplateListResponse>> getAllTemplates() {
        log.info("GET /api/templates - 전체 템플릿 목록 조회 요청");

        TemplateListResponse response = templateService.getAllTemplates();

        log.info("전체 템플릿 조회 완료 - 총 {}개", response.getTotalCount());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 템플릿 단건 조회
     * @param templateId 조회할 템플릿 ID
     * @return 템플릿 상세 정보
     */
    @GetMapping("/{templateId}")
    @Operation(
            summary = "특정 템플릿 조회",
            description = """
            템플릿 ID로 특정 템플릿의 상세 정보를 조회합니다.
            
            **사용 예시:**
```
            GET /api/templates/1
            
            Response:
            {
              "success": true,
              "message": "Success",
              "data": {
                "templateId": 1,
                "name": "Heart",
                "category": "romantic",
                "svgPath": "M 100,30 C 120,10 ...",
                "thumbnailUrl": null,
                "previewImageUrl": null,
                "avgDistance": 5.0,
                "pointCount": 100
              }
            }
```
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "템플릿을 찾을 수 없음"
            )
    })
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplateById(
            @Parameter(description = "템플릿 ID", example = "1")
            @PathVariable Long templateId
    ) {
        log.info("GET /api/templates/{} - 템플릿 조회 요청", templateId);

        TemplateResponse response = templateService.getTemplateById(templateId);

        log.info("템플릿 조회 완료 - templateId: {}, name: {}", templateId, response.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}