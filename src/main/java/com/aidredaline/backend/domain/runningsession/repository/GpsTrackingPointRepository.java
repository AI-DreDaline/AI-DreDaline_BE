package com.aidredaline.backend.domain.runningsession.repository;

import com.aidredaline.backend.domain.runningsession.entity.GpsTrackingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GpsTrackingPointRepository extends JpaRepository<GpsTrackingPoint, Long> {
    List<GpsTrackingPoint> findBySessionIdOrderByRecordedAtAsc(Long sessionId);
}
