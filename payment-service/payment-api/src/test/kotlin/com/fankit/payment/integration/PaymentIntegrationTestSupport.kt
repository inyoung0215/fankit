package com.fankit.payment.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * Payment Service 통합 테스트 베이스.
 *
 * - Testcontainers로 MySQL · Kafka · Redis 컨테이너를 1회 기동(JVM 단위 static)
 * - @DynamicPropertySource로 컨테이너 호스트/포트를 application context에 주입
 * - @SpringBootTest(RANDOM_PORT) + TestRestTemplate으로 실제 HTTP 호출
 *
 * 컨테이너는 JVM 1회만 부팅 — 클래스마다 재시작하지 않음 (속도 + 자원 절약).
 * 클래스 단위로 깨끗한 스키마가 필요한 경우 application-test.yml의 ddl-auto=create-drop이 처리.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
abstract class PaymentIntegrationTestSupport {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    protected fun url(path: String): String = "http://localhost:$port$path"

    companion object {
        // -- MySQL ----------------------------------------------------------------
        @JvmStatic
        private val mysql: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("fankit_payment")
            .withUsername("fankit")
            .withPassword("fankit")
            .withReuse(true)

        // -- Kafka (Confluent CP image) ------------------------------------------
        @JvmStatic
        private val kafka: KafkaContainer = KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
        ).withReuse(true)

        // -- Redis (분산 락 + Idempotency Cache) ---------------------------------
        // 로컬에 이미 있는 7.2-alpine 사용 (이미지 pull 시간 절약)
        @JvmStatic
        private val redis: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)

        init {
            // 순차 부팅: parallelStream + 컨테이너 readiness check는 ForkJoinPool common pool에서
            // 데드락이 날 수 있음 (Test Executor도 ForkJoin 사용). 첫 부팅은 다소 느려도 안정성 우선.
            // withReuse(true)로 두 번째 실행부터는 컨테이너 재사용 → 거의 즉시 준비됨.
            mysql.start()
            kafka.start()
            redis.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProps(registry: DynamicPropertyRegistry) {
            // MySQL
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.datasource.driver-class-name") { "com.mysql.cj.jdbc.Driver" }

            // Kafka — Spring Kafka 표준 키
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }

            // Redis (Spring Data Redis & Redisson 둘 다 spring.data.redis.* 읽음)
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }
}
