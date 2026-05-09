package com.fankit.payment.infrastructure.pg

import com.fankit.payment.domain.exception.PgUnavailableException
import com.fankit.payment.domain.port.outbound.PgApprovalRequest
import com.fankit.payment.domain.port.outbound.PgApprovalResponse
import com.fankit.payment.domain.port.outbound.PgCancelResponse
import com.fankit.payment.domain.port.outbound.PgClient
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

// 카카오페이 Mock — 실제 PG 연동 시뮬레이션
//
// Resilience4j Circuit Breaker:
//   instance "pgClient" 는 application.yml에 정의 (failureRateThreshold=50, waitDuration=30s)
//   임계 초과 시 OPEN 상태 진입 → fallback 메서드가 즉시 PgUnavailableException 발생
//   → Saga가 곧장 실패로 분기 (기다리지 않음 = "fail fast")
@Component
class MockKakaoPgClient : PgClient {
    private val log = LoggerFactory.getLogger(javaClass)

    @CircuitBreaker(name = "pgClient", fallbackMethod = "approveFallback")
    override fun approve(request: PgApprovalRequest): PgApprovalResponse {
        // Mock: 1만원 이상은 80% 성공, 아니면 100% 성공
        // 실전: HTTP 호출 + 응답 파싱
        if (request.amount >= 10_000 && Math.random() < 0.2) {
            throw RuntimeException("PG 승인 거절 (Mock 시뮬레이션)")
        }
        val txId = "kakao_${UUID.randomUUID().toString().take(12)}"
        log.info("[MockPG] approved orderId={} amount={} → {}", request.orderId, request.amount, txId)
        return PgApprovalResponse(pgTransactionId = txId, approvedAmount = request.amount)
    }

    @Suppress("unused")
    private fun approveFallback(request: PgApprovalRequest, ex: Throwable): PgApprovalResponse {
        // Circuit OPEN 또는 호출 실패 시 진입 — Saga에서 catch 가능하도록 도메인 예외로 wrap
        log.warn("[MockPG] circuit/error fallback for orderId={}: {}", request.orderId, ex.message)
        throw PgUnavailableException(ex.message ?: "PG 호출 실패")
    }

    @CircuitBreaker(name = "pgClient", fallbackMethod = "cancelFallback")
    override fun cancel(pgTransactionId: String): PgCancelResponse {
        log.info("[MockPG] cancelled tx={}", pgTransactionId)
        return PgCancelResponse(pgTransactionId, cancelledAmount = 0)
    }

    @Suppress("unused")
    private fun cancelFallback(pgTransactionId: String, ex: Throwable): PgCancelResponse {
        log.error("[MockPG] cancel fallback tx={} reason={}", pgTransactionId, ex.message)
        throw PgUnavailableException("PG 취소 실패: ${ex.message}")
    }
}
