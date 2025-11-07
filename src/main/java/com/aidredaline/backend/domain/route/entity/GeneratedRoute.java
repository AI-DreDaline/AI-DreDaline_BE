package com.aidredaline.backend.domain.route.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * 생성된 경로 엔티티
 * - 사용자가 템플릿을 선택하여 생성한 러닝 경로 정보 저장
 * - Flask AI 서버가 생성한 실제 도로 기반 경로를 저장
 * 4. 생성된 경로를 이 테이블에 저장
 *
 * DB : generated_routes
 */
@Entity
@Table(name = "generated_routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GeneratedRoute {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Integer routeId;

    @Column(name = "user_id")
    private Integer userId;


    @Column(name = "template_id")
    private Integer templateId;

    /**
     * 시작 지점 (PostGIS POINT)
     * ex) POINT(126.5312 33.4996) - 제주시청
     */
    @Column(name = "start_point", columnDefinition = "geometry(Point,4326)")
    private Point startPoint;

    /**
     * 생성된 경로 (PostGIS LINESTRING)
     * Flask AI 서버가 생성한 실제 도로를 따라가는 경로
     */
    @Column(name = "route_path", columnDefinition = "geometry(LineString,4326)")
    private LineString routePath;

    /**
     * 원본 템플릿 형태 (PostGIS LINESTRING)
     * 템플릿의 원래 모양 (비교를 위함)
     */
    @Column(name = "original_shape", columnDefinition = "geometry(LineString,4326)")
    private LineString originalShape;

    /**
     * 경로 미리보기 이미지 URL - 프론트에서 지도 위에 경로를 표시한 이미지
     */
    @Column(name = "route_preview_image_url", length = 500)
    private String routePreviewImageUrl;

    /**
     * 형태 오버레이 데이터 (JSONB)
     * 템플릿 모양과 실제 경로를 겹쳐서 보여주기 위한 데이터
     */
    @Column(name = "shape_overlay_data", columnDefinition = "jsonb")
        @JdbcTypeCode(SqlTypes.JSON) //PostgreSQL JSONB 타입이므로 @Type 지정 필요
        private String shapeOverlayData;

    // 실제 생성된 경로의 거리
    @Column(name = "total_distance", columnDefinition = "NUMERIC")
    private Double totalDistance;

    //평균 페이스 기준 예상 소요 시간 - 추후에 사용자 데이터 쌓이면 이용 가능
    @Column(name = "expected_duration")
    private Integer expectedDuration;

    /**
     * 유사도 점수 (0.0 ~ 1.0)
     * 템플릿 모양과 생성된 경로의 유사도
     */
    @Column(name = "similarity_score", columnDefinition = "NUMERIC")
    private Double similarityScore;


    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
