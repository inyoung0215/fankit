# FanKit — Claude Code 컨텍스트 파일

> 이 파일을 프로젝트 루트에 두면 Claude Code가 자동으로 읽어 컨텍스트로 활용합니다.

---

## 프로젝트 개요

**FanKit**은 K-POP 팬아트 크리에이터가 굿즈를 등록·판매하고, 팬이 안전하게 결제·구매할 수 있는 **MSA 기반 이커머스 플랫폼**입니다.

- **포인트**: 결제 시스템(Saga, Idempotency, Circuit Breaker), 정산 배치(Spring Batch), MSA 설계(Hexagonal + DDD)

---

## 기술 스택

### Language & Framework
- **Kotlin**
- **Spring Boot 3.x**
- **Spring Cloud** (Gateway, Eureka, Config)
- **Spring Data JPA + QueryDSL**
- **Spring Batch + Quartz** (Settlement Service)

### Database & Storage
| DB | 용도 |
|----|------|
| MySQL 8.0 | User, Payment, Settlement, Order |
| MongoDB | Goods (유연한 굿즈 스키마) |
| Redis | 세션, 캐시, 분산 락(Redisson) |
| Elasticsearch + Nori | 굿즈 한글 검색 |
| AWS S3 | 굿즈 이미지 |

### Messaging
- **Apache Kafka** — 서비스 간 이벤트 버스
- **Transactional Outbox Pattern** — 이벤트 유실 방지
- **Saga Pattern (Orchestration)** — 분산 트랜잭션

### Resilience
- **Resilience4j** — Circuit Breaker
- **Redisson 분산 락** — 재고 동시성 제어
- **비관적 락 (SELECT FOR UPDATE)** — 결제 중복 방지
- **Idempotency Key** — 결제 멱등성 보장

### Infra & DevOps
- Docker + Docker Compose
- GitHub Actions (CI/CD)
- AWS EC2, RDS, ElastiCache
- Prometheus + Grafana

### Testing & Docs
- JUnit 5 + **MockK** (Kotlin 네이티브)
- **Testcontainers** (통합 테스트)
- **Spring REST Docs** (Swagger 대신)

---

## 아키텍처 원칙

### 1. Hexagonal Architecture (Port & Adapter)
모든 서비스에 적용. 비즈니스 로직을 인프라에서 완전히 분리합니다.

```
{service}/
├── domain/           # 순수 도메인 모델, 비즈니스 로직 (외부 의존성 없음)
│   ├── model/        # Entity, Value Object, Aggregate
│   ├── port/         # UseCase 인터페이스 (inbound), Repository 인터페이스 (outbound)
│   └── event/        # Domain Event
├── application/      # UseCase 구현체, 서비스 오케스트레이션
├── infrastructure/   # DB, Kafka, Redis, S3 등 외부 어댑터 구현
└── api/              # Controller, DTO, Request/Response
```

### 2. DDD (도메인 주도 설계)
- Aggregate, Value Object, Domain Event 적극 활용
- 서비스 간 경계(Bounded Context)를 명확히 분리

### 3. Multi-Module Gradle
```
fankit/
├── common/               # 공통 모듈 (ApiResponse, Exception, BaseEntity)
├── user-service/
│   ├── user-domain/
│   ├── user-application/
│   ├── user-infrastructure/
│   └── user-api/
├── goods-service/
├── order-service/
├── payment-service/      # ⭐ 핵심
├── settlement-service/   # ⭐ 핵심
├── admin-service/
└── gateway/
```

---

## 서비스 정의

### 1. API Gateway
- **Tech**: Spring Cloud Gateway + Redis
- **역할**: 라우팅, JWT 검증, Rate Limiting (IP당 분당 100회)
- **공개 API** (인증 제외): 회원가입, 로그인, 굿즈 조회

### 2. User Service
- **Tech**: Kotlin + Spring Boot + MySQL + Redis
- **Domain**: User, Creator, Role
- **핵심 기능**:
  - JWT 이중 토큰 (Access 30분 / Refresh 7일)
  - Redis Refresh Token 저장 (탈취 시 즉시 무효화)
  - 크리에이터 전환 (관리자 승인 후 ROLE_CREATOR 부여)
- **주요 API**: `POST /api/v1/auth/signup`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`

### 3. Goods Service
- **Tech**: Kotlin + Spring Boot + MongoDB + Elasticsearch + Redis + S3
- **Domain**: Goods, GoodsOption, GoodsImage, Tag, Category
- **핵심 기능**:
  - MongoDB 유연한 스키마 (포토카드/키링/의류 등 다양한 굿즈 옵션)
  - Elasticsearch + Nori 한글 형태소 검색, 자동완성
  - Redis Sorted Set 인기 굿즈 랭킹 (Cache-Aside, TTL 5분)
  - S3 Pre-signed URL 이미지 업로드

### 4. Order Service
- **Tech**: Kotlin + Spring Boot + MySQL + Kafka
- **Domain**: Order, OrderItem, OrderStatus
- **상태 머신**: `CREATED → PAYMENT_PENDING → PAID → SHIPPING → DELIVERED / CANCELLED`
- **Kafka Consumer**: `payment.completed`, `payment.failed` 이벤트 수신

### 5. Payment Service ⭐ (면접 핵심)
- **Tech**: Kotlin + Spring Boot + MySQL + Kafka + Redis + Resilience4j
- **Domain**: Payment, PaymentMethod, PaymentStatus, IdempotencyKey, SagaLog
- **핵심 구현**:
  - **Idempotency Key**: 결제 API 중복 방지 (`X-Idempotency-Key` 헤더)
  - **비관적 락**: `SELECT FOR UPDATE` — 동일 주문 동시 결제 차단
  - **Saga (Orchestration)**: 결제 → 재고 차감 → 주문 확정, 실패 시 보상 트랜잭션
  - **Circuit Breaker**: Resilience4j, PG사 장애 시 OPEN 전이
  - **Redisson 분산 락**: 인기 굿즈 재고 동시성 제어
  - **Transactional Outbox**: DB 트랜잭션 + 이벤트 발행 원자성 보장
- **PG사**: 카카오페이 Mock API (실제 연동 시뮬레이션)

### 6. Settlement Service ⭐ (면접 핵심)
- **Tech**: Kotlin + Spring Boot + Spring Batch + Quartz + MySQL + Kafka
- **Domain**: Settlement, SettlementDetail, Reconciliation, Commission
- **핵심 구현**:
  - **Spring Batch**: ItemReader(전일 결제 조회) → ItemProcessor(수수료 10% 계산) → ItemWriter(DB 저장)
  - **Chunk 처리**: 100건 단위 (대용량 안정 처리)
  - **Quartz**: 매일 새벽 2시 자동 트리거
  - **대사(Reconciliation)**: PG사 정산 데이터 vs 내부 결제 데이터 비교 검증
  - 크리에이터 정산 내역 조회 API + CSV 다운로드

### 7. Admin Service
- **Tech**: Kotlin + Spring Boot + MySQL
- **Domain**: AdminUser, ApprovalLog, Statistics
- **기능**: 굿즈 승인/반려, 크리에이터 관리, 통계 대시보드

---

## 코딩 컨벤션

### Kotlin 스타일
```kotlin
// ✅ data class 적극 활용
data class CreateGoodsCommand(
    val creatorId: Long,
    val name: String,
    val price: Int,
    val options: List<GoodsOption>
)

// ✅ sealed class for 도메인 결과
sealed class PaymentResult {
    data class Success(val paymentId: Long, val amount: Int) : PaymentResult()
    data class Failure(val reason: String) : PaymentResult()
}

// ✅ Trailing lambda + 확장 함수 활용
// ✅ null safety 철저히 (!! 사용 금지)
// ✅ coroutine 고려 (필요 시)
```

### 레이어별 네이밍
| 레이어 | 클래스 suffix 예시 |
|--------|-------------------|
| domain/port (inbound) | `CreatePaymentUseCase`, `GetGoodsQuery` |
| domain/port (outbound) | `PaymentRepository`, `GoodsRepository` |
| application | `CreatePaymentService`, `GetGoodsService` |
| infrastructure | `PaymentJpaRepository`, `GoodsMongoRepository` |
| api | `PaymentController`, `CreatePaymentRequest`, `PaymentResponse` |

### API 응답 형식
```kotlin
// 공통 응답 (common 모듈)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
)

data class ErrorResponse(
    val code: String,
    val message: String
)
```

### 테스트 컨벤션 (MockK)
```kotlin
@Test
fun `결제 승인 시 Idempotency Key 중복이면 기존 결과를 반환한다`() {
    // given
    val idempotencyKey = "test-key-123"
    every { idempotencyRepository.findByKey(idempotencyKey) } returns existingPayment

    // when
    val result = createPaymentService.create(command, idempotencyKey)

    // then
    result shouldBe existingPayment
    verify(exactly = 0) { pgClient.approve(any()) }
}
```

---

## 개발 순서 (현재 단계)

```
[완료] Phase 0: AI 환경 셋업 (Cursor, Claude Code, CONVENTIONS.md)
[진행 중] Phase 1: 프로젝트 골격
  - Gradle Multi-Module 구조
  - Docker Compose 인프라
  - common 모듈

[다음] Phase 2: CRUD 서비스
  1. User Service (JWT/Redis)
  2. Goods Service (MongoDB/ES/Redis)
  3. Order Service
  4. Admin Service

[이후] Phase 3: 핵심 서비스 ⭐
  1. Payment Service (Saga/Idempotency/Circuit Breaker)
  2. Settlement Service (Spring Batch/Reconciliation)
  3. Transactional Outbox

[마무리] Phase 4: 통합·인프라
  - API Gateway
  - Testcontainers 통합 테스트
  - GitHub Actions CI/CD
  - Prometheus + Grafana
  - README + Spring REST Docs
```

---

## 면접 어필 포인트 (코드 작성 시 항상 염두)

Payment Service와 Settlement Service는 **면접 질문의 80%**를 차지합니다.
코드를 생성할 때 반드시 아래 질문에 답할 수 있도록 설계해주세요.

| 질문 | 구현 포인트 |
|------|------------|
| Saga Orchestration vs Choreography 왜 선택? | 명시적 흐름 추적, SagaLog 테이블로 가시성 확보 |
| Idempotency Key 어떻게 구현했나요? | DB unique key + Redis 캐시 이중 체크 |
| Circuit Breaker 임계값 설정 근거는? | failureRateThreshold=50, waitDuration=30s |
| 비관적 락 vs 분산 락 언제 쓰나요? | 단일 DB: 비관적 락 / 분산 환경: Redisson |
| Transactional Outbox 왜 필요한가요? | Kafka 직접 발행 시 DB 커밋 실패해도 이벤트 발행될 수 있음 |
| 1000만 건 정산 어떻게 처리하나요? | Chunk(100) + Partition + 병렬 Step |
| 대사 시스템이 왜 필요한가요? | PG사와 내부 데이터 불일치 자동 탐지 및 정합성 보장 |

---

## Docker Compose 인프라 구성

```yaml
# 참고: 전체 docker-compose.yml 구성 목록
services:
  mysql:      # User, Payment, Settlement, Order
  mongodb:    # Goods
  redis:      # Cache, Session, Redisson Lock
  kafka:      # Event Bus
  zookeeper:  # Kafka dependency
  elasticsearch: # Goods 검색
  kibana:     # ES 모니터링 (개발용)
  prometheus: # 메트릭 수집
  grafana:    # 대시보드
```

---

## Claude Code 사용 팁

1. **한 번에 하나씩 요청**: 서비스 하나, 기능 하나씩 요청해야 품질이 좋습니다.
2. **이 파일을 항상 참조**: "CLAUDE.md를 참고해서 Payment Service의 Saga Pattern을 구현해줘"
3. **Payment/Settlement는 생성 후 반드시 직접 이해**: 면접에서 코드 한 줄씩 설명할 수 있어야 합니다.
4. **테스트 코드 항상 함께 요청**: "구현 코드와 MockK 기반 단위 테스트를 함께 작성해줘"
5. **Hexagonal 명시**: 항상 "Hexagonal Architecture의 Port/Adapter 패턴으로" 명시

### 추천 프롬프트 템플릿
```
CLAUDE.md를 참고해서 {서비스명}의 {기능명}을 구현해줘.
- Kotlin + Spring Boot 3.x
- Hexagonal Architecture (Port/Adapter 패턴)
- domain / application / infrastructure / api 레이어 분리
- MockK 기반 단위 테스트 포함
- 면접에서 설명할 수 있도록 주요 설계 결정에 주석 추가
```

---

*FanKit CLAUDE.md v1.0 — 2026.05 기준*
