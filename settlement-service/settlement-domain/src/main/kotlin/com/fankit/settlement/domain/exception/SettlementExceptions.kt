package com.fankit.settlement.domain.exception

class SettlementNotFoundException(id: Long) :
    RuntimeException("정산을 찾을 수 없습니다: $id")

class SettlementJobAlreadyRunException(period: String) :
    RuntimeException("이미 처리된 정산 일자입니다: $period")
