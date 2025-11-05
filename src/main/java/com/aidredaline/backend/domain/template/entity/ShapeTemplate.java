package com.aidredaline.backend.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.LineString;

import java.time.LocalDateTime;

/**
 * 템플릿 엔티티
 * - 템플릿 정보 저장
 * - 사용자가 선택할 수 있는 템플릿 목록 제공

 * DB : shape_templates
 */
@Entity
@Table(name = "shape_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ShapeTemplate {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer templateId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String category;

    /**
     * 난이도 (예: "easy", "medium", "hard")
     * (향후 템플릿 자체의 복잡도 표시 등에 사용 가능)
     */
    @Column(length = 20)
    private String difficulty;

    /**
     * SVG 경로 데이터
     * 예: "M 100,30 C 120,10 180,10 200,30 ..."
     */
    @Column(name = "svg_path", columnDefinition = "TEXT", nullable = false)
    private String svgPath;

    /**
     * 템플릿의 실제 형태 (PostGIS LINESTRING)
     * Flask 서버에 전달하여 실제 경로 생성에 사용
     */
    @Column(name = "shape_geometry", columnDefinition = "geometry(LineString,4326)")
    private LineString shapeGeometry;

    /**
     * 썸네일 이미지 URL
     * 템플릿 목록에서 작은 이미지로 표시
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * 미리보기 이미지 URL
     * 템플릿 상세 화면에서 큰 이미지로 표시
     */
    @Column(name = "preview_image_url", length = 500)
    private String previewImageUrl;

    /**
     * 평균 거리 (km)
     * 이 템플릿으로 생성되는 경로의 예상 거리(참고용)
     */
    @Column(name = "avg_distance", columnDefinition = "NUMERIC")
    private Double avgDistance;

    /**
     * 포인트 개수
     * 템플릿을 구성하는 좌표점 개수
     * 복잡도 표시에 사용 가능
     */
    @Column(name = "point_count")
    private Integer pointCount;

    /**
     * 바운딩 박스 (경계 영역)
     * JSONB 형태로 저장
     *
     * 예시:
     * {
     *   "minLat": 33.4,
     *   "maxLat": 33.5,
     *   "minLng": 126.4,
     *   "maxLng": 126.6
     * }
     *
     * 용도: 지도에서 템플릿 전체가 보이도록 줌 레벨 계산
     */
    @Column(name = "bounding_box", columnDefinition = "jsonb")
    private String boundingBox;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}