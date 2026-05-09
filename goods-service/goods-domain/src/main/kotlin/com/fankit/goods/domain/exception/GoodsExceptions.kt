package com.fankit.goods.domain.exception

import com.fankit.goods.domain.model.GoodsStatus

class GoodsNotFoundException(id: String) :
    RuntimeException("굿즈를 찾을 수 없습니다: $id")

class IllegalGoodsStateTransitionException(from: GoodsStatus, to: GoodsStatus) :
    RuntimeException("굿즈 상태 전이 불가: $from → $to")

class ForbiddenGoodsAccessException(reason: String) :
    RuntimeException("굿즈 접근 권한 없음: $reason")
