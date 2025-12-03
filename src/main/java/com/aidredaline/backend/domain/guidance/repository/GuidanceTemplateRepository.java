package com.aidredaline.backend.domain.guidance.repository;

import com.aidredaline.backend.domain.guidance.entity.GuidanceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuidanceTemplateRepository extends JpaRepository<GuidanceTemplate, String> {

    // guidance_id로 조회
    Optional<GuidanceTemplate> findByGuidanceId(String guidanceId);

    // 카테고리별 조회
    List<GuidanceTemplate> findByCategory(String category);
}