package com.fankit.user.domain.exception

// 도메인 예외 — common.exception에 의존하지 않음 (Hexagonal: 도메인은 외부 모듈에 의존하지 않는다)
// API 레이어의 GlobalExceptionHandler에서 HTTP 응답으로 매핑

class EmailAlreadyExistsException(email: String) :
    RuntimeException("이미 가입된 이메일입니다: $email")

class InvalidCredentialsException :
    RuntimeException("이메일 또는 비밀번호가 일치하지 않습니다")

class InvalidRefreshTokenException :
    RuntimeException("유효하지 않은 리프레시 토큰입니다")
