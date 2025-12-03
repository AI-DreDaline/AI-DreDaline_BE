package com.aidredaline.backend.domain.route.repository;

import com.aidredaline.backend.domain.route.entity.GeneratedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * - findById(Integer id): 경로 ID로 조회
 * - findAll(): 전체 경로 조회
 * - save(GeneratedRoute): 경로 저장
 * - delete(GeneratedRoute): 경로 삭제
 */
@Repository
public interface GeneratedRouteRepository extends JpaRepository<GeneratedRoute, Integer> {

    /**
     * 사용자별 경로 목록 조회
     * 최신순으로 정렬
     */
    List<GeneratedRoute> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // 템플릿별 경로 조회
    List<GeneratedRoute> findByTemplateId(Integer templateId);

    /**
     * 사용자 + 템플릿 조합으로 조회
     * "이 사용자가 이 템플릿으로 만든 경로들"
     */
    List<GeneratedRoute> findByUserIdAndTemplateId(Integer userId, Integer templateId);

    //저장된 경로만 조회 (is_saved = true)인것만.
    List<GeneratedRoute> findByUserIdAndIsSavedOrderByCreatedAtDesc(
            Integer userId,
            Boolean isSaved
    );
}