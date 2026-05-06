package com.fankit.user.infrastructure.persistence

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// JPA 관련 설정은 모두 infrastructure 모듈에 위치 — api 모듈은 spring-data-jpa를 직접 의존하지 않음
// @EnableJpaAuditing: BaseEntity의 @CreatedDate/@LastModifiedDate 자동 채움
@Configuration
@EntityScan(basePackages = ["com.fankit.user.infrastructure.persistence", "com.fankit.common.entity"])
@EnableJpaRepositories(basePackages = ["com.fankit.user.infrastructure.persistence"])
@EnableJpaAuditing
class JpaConfig
