package com.fankit.settlement.integration

import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * Settlement Service 통합 테스트 베이스.
 *
 * - @SpringBootTest(NONE): Web 띄울 필요 없음 (Batch Job만 검증)
 * - @SpringBatchTest: JobLauncherTestUtils · JobRepositoryTestUtils 자동 등록
 * - Testcontainers MySQL + Kafka — JVM 1회 부팅(static), @DynamicPropertySource 주입
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SpringBatchTest
@ActiveProfiles("test")
@Testcontainers
abstract class SettlementIntegrationTestSupport {

    companion object {
        @JvmStatic
        private val mysql: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("fankit_settlement")
            .withUsername("fankit")
            .withPassword("fankit")
            .withReuse(true)

        @JvmStatic
        private val kafka: KafkaContainer = KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
        ).withReuse(true)

        init {
            // 순차 부팅 (parallelStream은 ForkJoinPool 데드락 위험)
            mysql.start()
            kafka.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.datasource.driver-class-name") { "com.mysql.cj.jdbc.Driver" }
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }
    }
}
