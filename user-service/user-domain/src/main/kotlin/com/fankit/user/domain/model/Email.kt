package com.fankit.user.domain.model

@JvmInline
value class Email(val value: String) {
    init {
        require(value.matches(EMAIL_REGEX)) { "잘못된 이메일 형식: $value" }
    }
    companion object {
        private val EMAIL_REGEX = Regex("""^[A-Za-z0-9+_.\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$""")
    }
}
