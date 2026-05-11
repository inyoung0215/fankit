package com.fankit.admin.domain.port.inbound

import com.fankit.admin.domain.model.AdminDecisionLog

interface ApproveGoodsUseCase {
    fun approve(goodsId: String, adminId: Long): AdminDecisionLog
}

interface RejectGoodsUseCase {
    fun reject(goodsId: String, adminId: Long, reason: String): AdminDecisionLog
}
