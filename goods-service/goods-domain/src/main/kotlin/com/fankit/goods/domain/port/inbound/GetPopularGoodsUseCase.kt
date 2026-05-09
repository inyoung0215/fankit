package com.fankit.goods.domain.port.inbound

import com.fankit.goods.domain.model.Goods

interface GetPopularGoodsUseCase {
    fun topN(n: Int): List<Goods>
}
