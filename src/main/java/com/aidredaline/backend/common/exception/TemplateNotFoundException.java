package com.aidredaline.backend.common.exception;

import jakarta.persistence.EntityNotFoundException;

/**
 * 템플릿을 찾을 수 없을 때
 *
 * 사용 시점:
 * - 존재하지 않는 templateId로 조회 시도 시
 *
 * 결과:
 * - GlobalExceptionHandler의 handleEntityNotFound() 메서드가 처리
 * - 404 NOT_FOUND 응답 반환
 */
public class TemplateNotFoundException extends EntityNotFoundException {
    public TemplateNotFoundException(Long templateId) {
        super("해당 템플릿을 찾을 수 없습니다. 템플릿ID: " + templateId);
    }
}