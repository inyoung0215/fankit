package com.fankit.payment.domain.port.outbound

import java.time.Duration

// Redisson 기반 분산 락 (Pub/Sub + Lua script로 atomic)
//
// 비관적 락 vs 분산 락:
//   비관적 락 = 단일 DB 인스턴스 내 row lock (SELECT FOR UPDATE)
//              FanKit Payment Service는 같은 orderId의 결제 중복을 막기 위해 사용
//   분산 락   = 여러 서비스/인스턴스가 공유 자원에 접근할 때 (예: 인기 굿즈 재고)
//              Goods 인스턴스 N대가 동일 굿즈 재고를 동시 차감하면 over-sell
//              → Redis key 기반 mutex로 직렬화
//
// 둘 중 무엇? "DB scope면 비관적 락, cluster scope면 분산 락"
interface DistributedLock {
    fun <T> withLock(key: String, waitTime: Duration, leaseTime: Duration, action: () -> T): T
}
