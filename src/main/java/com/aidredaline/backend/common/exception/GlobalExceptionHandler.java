package com.aidredaline.backend.common.exception;

import com.aidredaline.backend.common.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 1. Controller에서 예외 발생
 * 2. Spring이 자동으로 이 핸들러의 적절한 @ExceptionHandler 메서드 호출
 * 3. 통일된 에러 응답 반환
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 엔티티를 찾지 못했을 때
     * - 존재하지 않는 templateId로 조회 시도
     * - 존재하지 않는 routeId로 조회 시도
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(EntityNotFoundException e) {
        log.error("Entity not found: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)  // 404
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 잘못된 인자가 전달되었을 때
     * - 유효하지 않은 파라미터 전달
     * - 비즈니스 로직 위반
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * Bean Validation 실패
     * - @NotNull, @Min, @Max 등의 검증 어노테이션 위반
     * - DTO 필드 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException e
    ) {
        // 모든 검증 실패 메시지를 Map으로 수집
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400
                .body(ApiResponse.error("Validation failed"));
    }

    /**
     * 위에서 처리하지 못한 모든 예외를 처리 - 폴백 핸들러
     * : 서버가 죽지 않고 에러 응답을 반환하고 에러 로그 남기도록
     *
     * - 예상하지 못한 예외
     * - NullPointerException 등
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception e) {
        log.error("Unexpected error", e);  // 전체 스택 트레이스 로깅
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
                .body(ApiResponse.error("Internal server error: " + e.getMessage()));
    }
}