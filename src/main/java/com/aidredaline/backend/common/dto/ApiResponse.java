package com.aidredaline.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 모든 API 응답의 공통 포맷
 * 응답 예시:
 * {
 *   "success": true,
 *   "message": "Success",
 *   "data": { ... }
 * }
 *
 * 사용 예시:
 * return ResponseEntity.ok(ApiResponse.success(템플릿목록));
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 값은 JSON에서 제외
public class ApiResponse<T> {

    private boolean success;
    private String message;

    /**
     * 실제 응답 데이터
     */
    private T data;
    /**
     * 성공 응답 생성 (기본 메시지)
     * @param data 응답 데이터
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * 성공 응답 생성 (커스텀 메시지)
     * @param message 커스텀 성공 메시지
     * @param data 응답 데이터
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * 에러 응답 생성
     * @param message 에러 메시지
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}