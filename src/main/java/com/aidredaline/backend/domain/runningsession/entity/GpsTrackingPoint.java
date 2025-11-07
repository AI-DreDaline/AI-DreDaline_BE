package com.aidredaline.backend.domain.runningsession.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "gps_tracking_points")
@Getter @Setter
public class GpsTrackingPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Integer pointId;

    @Column(name = "session_id")
    private Integer sessionId;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Column(name = "recorded_at")
    private Instant recordedAt;

    private BigDecimal accuracy;
    private BigDecimal speed;
    private BigDecimal altitude;
}
