package com.aidredaline.backend.external.flask;

import com.aidredaline.backend.external.flask.dto.FlaskRouteRequest;
import com.aidredaline.backend.external.flask.dto.FlaskRouteResponse;

/**
 * - Flask 서버와의 통신 추상화
 * - MockFlaskClient: 개발용 (Flask 없이 테스트)
 * - RealFlaskClient: 운영용 (실제 Flask 호출) - 개발 예정
 */
public interface FlaskClient {

    /**
     * 경로 생성 요청
     * @param request Flask 요청 DTO
     * @return Flask 응답 DTO
     */
    FlaskRouteResponse generateRoute(FlaskRouteRequest request);
}