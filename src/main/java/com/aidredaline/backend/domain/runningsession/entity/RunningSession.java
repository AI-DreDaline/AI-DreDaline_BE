package com.aidredaline.backend.domain.runningsession.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "running_sessions")
@Getter @Setter
public class RunningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "route_id")
    private Integer routeId;

    private String status;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "actual_distance")
    private BigDecimal actualDistance;

    @Column(name = "moving_time")
    private Integer movingTime;

    @Column(name = "total_paused_duration")
    private Integer totalPausedDuration;

    @Column(name = "average_pace")
    private BigDecimal averagePace;

    @Column(columnDefinition = "geometry(LineString, 4326)")
    private LineString actualPath;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point currentPosition;

    @Type(com.vladmihalcea.hibernate.type.json.JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> pauseHistory;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) status = "in_progress";
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
