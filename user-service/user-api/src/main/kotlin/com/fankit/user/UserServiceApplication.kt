package com.fankit.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

// JPA 설정(@EnableJpaAuditing/@EnableJpaRepositories/@EntityScan)은
// infrastructure 모듈의 JpaConfig에서 처리 — api는 JPA를 직접 의존하지 않는다
@SpringBootApplication
@EnableDiscoveryClient
class UserServiceApplication

fun main(args: Array<String>) {
    runApplication<UserServiceApplication>(*args)
}
