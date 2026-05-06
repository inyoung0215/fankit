plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":settlement-service:settlement-domain"))
    implementation(project(":settlement-service:settlement-application"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.mysql:mysql-connector-j")

    // Spring Batch — 정산 배치 (Chunk 단위 처리)
    implementation("org.springframework.boot:spring-boot-starter-batch")
    // Quartz — 매일 새벽 2시 자동 트리거
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    // Kafka — 정산 완료 이벤트 발행
    implementation("org.springframework.kafka:spring-kafka")

    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.batch:spring-batch-test")
}
