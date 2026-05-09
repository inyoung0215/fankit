package com.fankit.goods.domain.model

// 굿즈 상태 머신
//
//   PENDING_APPROVAL ──approve()──▶ APPROVED ──markSoldOut()──▶ SOLD_OUT
//        │                              │                          │
//        └──reject()──▶ REJECTED        └──discontinue()──▶ DISCONTINUED ◀┘
//
enum class GoodsStatus {
    PENDING_APPROVAL,   // 크리에이터 등록 직후 (관리자 승인 대기)
    APPROVED,           // 승인 완료, 판매 중
    REJECTED,           // 반려
    SOLD_OUT,           // 품절
    DISCONTINUED,       // 판매 중지
}
