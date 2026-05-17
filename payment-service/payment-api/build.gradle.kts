plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":payment-service:payment-domain"))
    implementation(project(":payment-service:payment-application"))
    implementation(project(":payment-service:payment-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // === Integration test ===
    // @SpringBootTest 전체 컨텍스트 + Testcontainers (MySQL/Kafka/Redis)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // 테스트에서 검증 도구로 JpaRepository 직접 사용 (Outbox/SagaLog row 확인용)
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.awaitility:awaitility:4.2.1")  // Outbox 비동기 발행 대기
    // Payment 모듈 전체가 컨텍스트에 로드되므로 인프라/JPA 어댑터까지 의존
    testImplementation(project(":payment-service:payment-infrastructure"))
}
