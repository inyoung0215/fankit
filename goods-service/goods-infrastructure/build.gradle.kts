plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":goods-service:goods-domain"))
    implementation(project(":goods-service:goods-application"))

    // MongoDB — 굿즈 유연한 스키마
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    // Elasticsearch — 한글 형태소 검색 (Nori)
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    // Redis — 인기 굿즈 랭킹 캐시
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // S3 — 굿즈 이미지
    implementation("software.amazon.awssdk:s3:2.25.30")

    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:elasticsearch")
    testImplementation("org.testcontainers:junit-jupiter")
}
