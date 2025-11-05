package com.aidredaline.backend.domain.template.dto;

import com.aidredaline.backend.domain.template.entity.ShapeTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 템플릿 응답 DTO
 * - 프론트에 템플릿 정보 전달
 * - GET /api/templates (전체 목록)
 * - GET /api/templates/{id} (단건 조회)
 */
@Getter
@Builder
@AllArgsConstructor
public class TemplateResponse {

    private Long templateId;
    private String name;
    private String category;
    private String svgPath;
    private String thumbnailUrl;
    private String previewImageUrl;
    private Double avgDistance;
    private Integer pointCount;

    //Entity → DTO
    public static TemplateResponse from(ShapeTemplate template) {
        return TemplateResponse.builder()
                .templateId(template.getTemplateId())
                .name(template.getName())
                .category(template.getCategory())
                .svgPath(template.getSvgPath())
                .thumbnailUrl(template.getThumbnailUrl())
                .previewImageUrl(template.getPreviewImageUrl())
                .avgDistance(template.getAvgDistance())
                .pointCount(template.getPointCount())
                .build();
    }
}