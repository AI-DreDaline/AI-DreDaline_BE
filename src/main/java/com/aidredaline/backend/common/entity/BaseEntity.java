package com.aidredaline.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티가 상속 받도록
 * 목적:모든 테이블에 공통으로 필요한 created_at, updated_at 컬럼을 자동 관리
 */
@MappedSuperclass           // 이 클래스는 테이블로 생성되지 않고, 자식 클래스의 컬럼으로 포함됨
@Getter
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing 기능 활성화
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * @LastModifiedDate: 엔티티가 수정될 때마다 자동으로 현재 시각으로 업데이트
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}