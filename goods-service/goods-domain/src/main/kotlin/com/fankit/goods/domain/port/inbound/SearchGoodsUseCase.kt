package com.fankit.goods.domain.port.inbound

import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.Goods

interface SearchGoodsUseCase {
    fun search(query: SearchGoodsQuery): SearchGoodsResult
}

data class SearchGoodsQuery(
    val keyword: String? = null,
    val category: Category? = null,
    val page: Int = 0,
    val size: Int = 20,
)

data class SearchGoodsResult(
    val items: List<Goods>,
    val totalHits: Long,
    val page: Int,
    val size: Int,
)
