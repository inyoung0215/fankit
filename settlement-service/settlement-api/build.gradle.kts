plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":settlement-service:settlement-domain"))
    implementation(project(":settlement-service:settlement-application"))
    implementation(project(":settlement-service:settlement-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // === Integration test ===
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // JpaRepository 직접 사용 (Settlement/Detail row 검증)
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Spring Batch Test — JobLauncherTestUtils
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:junit-jupiter")
    // 전체 컨텍스트 로드 시 infrastructure 클래스 필요
    testImplementation(project(":settlement-service:settlement-infrastructure"))
}
