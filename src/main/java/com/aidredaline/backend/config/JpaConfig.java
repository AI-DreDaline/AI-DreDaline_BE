package com.aidredaline.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정 클래스
 * 목적:
 * - @CreatedDate, @LastModifiedDate 어노테이션이 작동하도록 설정
 * - BaseEntity의 created_at, updated_at 자동 관리 기능 활성화
 */
@Configuration
@EnableJpaAuditing  // JPA Auditing 기능 활성화
public class JpaConfig {
}