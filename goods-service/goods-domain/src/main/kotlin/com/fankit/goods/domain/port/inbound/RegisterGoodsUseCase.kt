package com.fankit.goods.domain.port.inbound

import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.GoodsOption

interface RegisterGoodsUseCase {
    fun register(command: RegisterGoodsCommand): String   // returns goods id
}

data class RegisterGoodsCommand(
    val creatorId: Long,
    val name: String,
    val description: String,
    val basePrice: Int,
    val category: Category,
    val tags: List<String>,
    val options: List<GoodsOption>,
    val imageUrls: List<String>,
)
