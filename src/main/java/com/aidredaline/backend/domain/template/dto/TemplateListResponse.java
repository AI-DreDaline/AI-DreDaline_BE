package com.aidredaline.backend.domain.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 템플릿 목록 응답 DTO
 * - 전체 템플릿 목록 + 메타 정보 전달
 * - 프론트에서 목록 렌더링 및 통계 표시에 사용
 *
 * GET /api/templates (전체 목록 조회)
 */
@Getter
@Builder
@AllArgsConstructor
public class TemplateListResponse {

    private List<TemplateResponse> templates;

    private int totalCount;

    public static TemplateListResponse of(List<TemplateResponse> templates) {
        return TemplateListResponse.builder()
                .templates(templates)
                .totalCount(templates.size())
                .build();
    }
}