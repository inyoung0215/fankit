plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":user-service:user-domain"))
    implementation("org.springframework.boot:spring-boot-starter")
    // @Transactional 어노테이션 — spring-tx는 spring-context의 transitive가 아니므로 명시 필요
    implementation("org.springframework:spring-tx")
}
