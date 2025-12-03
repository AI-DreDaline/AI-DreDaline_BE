package com.aidredaline.backend.domain.guidance.repository;

import com.aidredaline.backend.domain.guidance.entity.TurnPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurnPointRepository extends JpaRepository<TurnPoint, Integer> {

    // 특정 경로의 턴바이턴 지점 조회 (순서대로)
    List<TurnPoint> findByRouteIdOrderBySequenceAsc(Integer routeId);

    // 특정 경로의 턴바이턴 개수
    Long countByRouteId(Integer routeId);

    // 특정 경로의 턴바이턴 삭제
    void deleteByRouteId(Integer routeId);
}