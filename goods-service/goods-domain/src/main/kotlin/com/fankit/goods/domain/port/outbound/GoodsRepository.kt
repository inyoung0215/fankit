package com.fankit.goods.domain.port.outbound

import com.fankit.goods.domain.model.Goods

// MongoDB 영속화 추상화 (Hexagonal: 도메인이 Mongo를 모르도록)
interface GoodsRepository {
    fun save(goods: Goods): Goods
    fun findById(id: String): Goods?
    fun deleteById(id: String)
    fun incrementViewCount(id: String)
}
