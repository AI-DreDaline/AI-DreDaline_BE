package com.aidredaline.backend.common.exception;

import jakarta.persistence.EntityNotFoundException;

/**
 * 경로를 찾을 수 없을 때 발생시키는 예외
 *
 * 사용 시점:
 * - 존재하지 않는 routeId로 조회 시도 시
 *
 * 결과:
 * - 404 NOT_FOUND 응답
 * - 메시지: "Route not found with id: 123"
 */
public class RouteNotFoundException extends EntityNotFoundException {
    public RouteNotFoundException(String message) {
        super(message);
    }

    public RouteNotFoundException(Integer routeId) {
        super("해당 경로는 찾을 수 없습니다. 경로ID: " + routeId);
    }
}