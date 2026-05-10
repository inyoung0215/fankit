plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":settlement-service:settlement-domain"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")    // @Transactional
}
