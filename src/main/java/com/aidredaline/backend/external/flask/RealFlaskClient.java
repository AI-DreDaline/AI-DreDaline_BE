package com.aidredaline.backend.external.flask;

import com.aidredaline.backend.common.exception.FlaskServerException;
import com.aidredaline.backend.external.flask.dto.FlaskRouteRequest;
import com.aidredaline.backend.external.flask.dto.FlaskRouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Flask Real 클라이언트 (운영용)
 * : 실제 Flask AI 서버와 통신
 * : RestTemplate으로 HTTP 요청
 *
 * 활성화:
 * - @Profile("prod"): 운영 환경에서만 사용
 */
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
@Slf4j
public class RealFlaskClient implements FlaskClient {

    private final RestTemplate restTemplate;

    @Value("${flask.server.url}")
    private String flaskServerUrl;

    @Override
    public FlaskRouteResponse generateRoute(FlaskRouteRequest request) {
        log.info("RealFlaskClient - Flask 서버 호출 시작");
        log.info("URL: {}/routes/generate", flaskServerUrl);
        log.info("요청: templateName={}, targetKm={}, startPoint=({}, {})",
                request.getTemplateName(),
                request.getTargetKm(),
                request.getStartPoint().getLat(),
                request.getStartPoint().getLng());

        try {
            String url = flaskServerUrl + "/routes/generate";

            FlaskRouteResponse response = restTemplate.postForObject(
                    url,
                    request,
                    FlaskRouteResponse.class
            );

            if (response == null || !Boolean.TRUE.equals(response.getOk())) {
                throw new FlaskServerException("Flask 서버 응답 실패");
            }

            log.info("RealFlaskClient - Flask 응답 성공");
            log.info("생성된 노드 수: {}", response.getData().getMetrics().getNodes());

            //음성 안내 포인트 로그
            if (response.getData().getGuidance() != null) {
                int guidanceCount = response.getData().getGuidance().getGuidancePoints().size();
                log.info("음성 안내 포인트 수: {}", guidanceCount);
            }

            return response;

        } catch (RestClientException e) {
            log.error("Flask 서버 통신 실패", e);
            throw new FlaskServerException("Flask 서버 통신 실패: " + e.getMessage(), e);
        }
    }
}