package com.aidredaline.backend.common.exception;

/**
 * Flask AI 서버와의 통신 중 오류 발생 시
 *
 * 사용 시점:
 * - Flask 서버가 응답하지 않을 때
 * - Flask 서버가 500 에러를 반환할 때
 * - 네트워크 연결 실패 시
 *
 * 결과:
 * - GlobalExceptionHandler의 handleGeneral() 메서드가 처리
 * - 500 INTERNAL_SERVER_ERROR 응답
 * - 프론트에 적절한 에러 메시지 전달
 */
public class FlaskServerException extends RuntimeException {

    public FlaskServerException(String message) {
        super(message);
    }

    public FlaskServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
