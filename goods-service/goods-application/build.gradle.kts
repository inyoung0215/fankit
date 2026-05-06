plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":goods-service:goods-domain"))
    implementation("org.springframework.boot:spring-boot-starter")
}
