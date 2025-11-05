package com.aidredaline.backend.domain.template.service;

import com.aidredaline.backend.common.exception.TemplateNotFoundException;
import com.aidredaline.backend.domain.template.dto.TemplateListResponse;
import com.aidredaline.backend.domain.template.dto.TemplateResponse;
import com.aidredaline.backend.domain.template.entity.ShapeTemplate;
import com.aidredaline.backend.domain.template.repository.ShapeTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TemplateService {

    private final ShapeTemplateRepository templateRepository;

    /**
     * 전체 템플릿 목록 조회
     * GET /api/templates
     * @return 전체 템플릿 목록 + 메타 정보
     */
    public TemplateListResponse getAllTemplates() {
        log.info("전체 템플릿 조회 시작");

        // 1. DB에서 전체 템플릿 조회
        List<ShapeTemplate> templates = templateRepository.findAll();
        log.info("조회된 템플릿 개수: {}", templates.size());

        // 2.Entity → DTO
        List<TemplateResponse> templateResponses = templates.stream()
                .map(TemplateResponse::from)
                .collect(Collectors.toList());

        return TemplateListResponse.of(templateResponses);
    }

    /**
     *  특정 템플릿 단건 조회(경로 생성 시 선택한 템플릿 정보 가져오기)
     * - GET /api/templates/{id}
     * @param templateId 조회할 템플릿 ID
     * @return 템플릿 정보
     * @throws TemplateNotFoundException 템플릿이 존재하지 않을 경우
     */
    public TemplateResponse getTemplateById(Long templateId) {
        log.info("템플릿 조회 시작 - templateId: {}", templateId);

        // DB에서 조회, 없으면 예외 발생
        ShapeTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> {
                    log.error("템플릿을 찾을 수 없음 - templateId: {}", templateId);
                    return new TemplateNotFoundException(templateId);
                });

        log.info("템플릿 조회 성공 - name: {}", template.getName());
        return TemplateResponse.from(template);
    }

    /**
     * 카테고리별 템플릿 조회 - 카테고리 필터링 기능 추가 시 사용
     * @param category 카테고리 (예: "romantic", "fun")
     * @return 해당 카테고리의 템플릿 목록
     */
//    public TemplateListResponse getTemplatesByCategory(String category) {
//        log.info("카테고리별 템플릿 조회 시작 - category: {}", category);
//
//        List<ShapeTemplate> templates = templateRepository.findByCategory(category);
//        log.info("조회된 템플릿 개수: {}", templates.size());
//
//        List<TemplateResponse> templateResponses = templates.stream()
//                .map(TemplateResponse::from)
//                .collect(Collectors.toList());
//
//        return TemplateListResponse.of(templateResponses);
//    }

    /**
     * 템플릿 검색
     * @param keyword 검색 키워드
     * @return 검색 결과 템플릿 목록
     */
//    public TemplateListResponse searchTemplates(String keyword) {
//        log.info("템플릿 검색 시작 - keyword: {}", keyword);
//
//        List<ShapeTemplate> templates = templateRepository.findByNameContainingIgnoreCase(keyword);
//        log.info("검색 결과 개수: {}", templates.size());
//
//        List<TemplateResponse> templateResponses = templates.stream()
//                .map(TemplateResponse::from)
//                .collect(Collectors.toList());
//
//        return TemplateListResponse.of(templateResponses);
//    }

    /**
     * 내부 메서드: Entity 조회 (경로 생성 시 사용)
     *
     * 목적:
     * - (Flask)RouteService에서 경로 생성 시 템플릿 Entity가 필요
     * - DTO가 아닌 Entity를 반환
     */
    public ShapeTemplate getTemplateEntityById(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));
    }
}
