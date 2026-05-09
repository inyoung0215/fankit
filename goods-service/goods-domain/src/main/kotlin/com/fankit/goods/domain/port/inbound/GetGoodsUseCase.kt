package com.fankit.goods.domain.port.inbound

import com.fankit.goods.domain.model.Goods

interface GetGoodsUseCase {
    // 단건 조회 + 조회수 증가 (서버에서 주관적 시점에 +1)
    // 면접 어필: 인기 랭킹은 Redis Sorted Set, 영구 카운트는 Mongo — 이중 갱신
    fun getById(id: String): Goods
}
