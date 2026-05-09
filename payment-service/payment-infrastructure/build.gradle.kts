plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":payment-service:payment-domain"))
    implementation(project(":payment-service:payment-application"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.mysql:mysql-connector-j")

    // Kafka — Transactional Outbox 이벤트 발행
    implementation("org.springframework.kafka:spring-kafka")

    // Redis + Redisson — 분산 락 (재고 동시성 제어)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson-spring-boot-starter:3.27.2")

    // Resilience4j @CircuitBreaker 어노테이션 — MockKakaoPgClient에서 사용
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")

    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
