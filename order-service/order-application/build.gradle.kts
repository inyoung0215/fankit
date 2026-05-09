plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":order-service:order-domain"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")    // @Transactional
}
