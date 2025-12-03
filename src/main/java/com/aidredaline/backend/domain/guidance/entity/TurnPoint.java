package com.aidredaline.backend.domain.guidance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "turn_points")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "turn_point_id")
    private Integer turnPointId;

    @Column(name = "route_id", nullable = false)
    private Integer routeId;

    @Column(nullable = false)
    private Integer sequence;

    // 위치 정보
    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    // 회전 정보
    @Column(length = 20, nullable = false)
    private String direction;  // "left", "right", "straight", "u_turn"

    private Double angle;

    // 거리 정보
    @Column(name = "distance_from_start", precision = 10, scale = 2)
    private BigDecimal distanceFromStart;

    @Column(name = "distance_to_next", precision = 10, scale = 2)
    private BigDecimal distanceToNext;

    // 안내 정보
    @Column(name = "guidance_id", length = 50, nullable = false)
    private String guidanceId;  // "TURN_LEFT_50"

    @Column(name = "trigger_distance")
    private Double triggerDistance = 15.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}