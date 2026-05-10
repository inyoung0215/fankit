package com.fankit.settlement.domain.model

enum class SettlementStatus {
    PROCESSING,    // 배치 진행 중
    COMPLETED,     // 정산 완료
    FAILED,        // 배치 실패 (운영 알람 + 재실행 필요)
}
