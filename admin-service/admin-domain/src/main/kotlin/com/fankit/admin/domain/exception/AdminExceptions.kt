package com.fankit.admin.domain.exception

class ExternalServiceCallFailedException(target: String, reason: String) :
    RuntimeException("외부 서비스 호출 실패: $target — $reason")
