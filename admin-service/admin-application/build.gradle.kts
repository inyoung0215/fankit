plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":admin-service:admin-domain"))
    implementation("org.springframework.boot:spring-boot-starter")
}
