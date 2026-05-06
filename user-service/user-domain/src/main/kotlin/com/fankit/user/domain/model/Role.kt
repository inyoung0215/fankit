package com.fankit.user.domain.model

enum class Role {
    USER,      // 기본 가입자 (구매자)
    CREATOR,   // 굿즈 등록·판매 권한 보유 (관리자 승인 후 부여)
    ADMIN,
}
