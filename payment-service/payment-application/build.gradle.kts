plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":payment-service:payment-domain"))
    implementation("org.springframework.boot:spring-boot-starter")

    // Resilience4j — Circuit Breaker (PG사 호출 보호)
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")
}
