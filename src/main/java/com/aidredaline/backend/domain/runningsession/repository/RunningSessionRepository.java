package com.aidredaline.backend.domain.runningsession.repository;

import com.aidredaline.backend.domain.runningsession.entity.RunningSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RunningSessionRepository extends JpaRepository<RunningSession, Long> {

    Page<RunningSession> findByUserIdAndStatusOrderByStartTimeDesc(Long userId, String status, Pageable pageable);

    @Query("SELECT COUNT(s), COALESCE(SUM(s.actualDistance), 0), COALESCE(AVG(s.averagePace), 0) " +
           "FROM RunningSession s WHERE s.userId = :userId AND s.status = 'completed'")
    Object[] getStatisticsSummary(Long userId);
}
