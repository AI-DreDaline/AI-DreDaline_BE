package com.aidredaline.backend.domain.template.repository;

import com.aidredaline.backend.domain.template.entity.ShapeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JpaRepository가 기본 제공하는 메서드:
 * - findAll(): 전체 템플릿 조회
 * - findById(Long id): ID로 단건 조회
 * - save(ShapeTemplate): 저장/수정
 * - delete(ShapeTemplate): 삭제
 * - count(): 전체 개수
 */
@Repository
public interface ShapeTemplateRepository extends JpaRepository<ShapeTemplate, Long> {

    /**
     * (나중에 필요하면)
     * 카테고리별 템플릿 조회
     */
    List<ShapeTemplate> findByCategory(String category);

    /**
     * (추후에 검색 기능 들어가면)
     * 이름으로 검색 (부분 일치, 대소문자 무시)
     */
    List<ShapeTemplate> findByNameContainingIgnoreCase(String name);
}