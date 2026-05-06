package com.fankit.user.domain.model

// 도메인은 raw password를 모른다 — 항상 인코딩된 형태로만 다룸으로써
// 평문 패스워드가 도메인 모델/로그/직렬화에 노출되는 사고를 원천 차단
@JvmInline
value class EncodedPassword(val value: String) {
    init {
        require(value.isNotBlank()) { "암호화된 패스워드는 비어있을 수 없다" }
    }
}
