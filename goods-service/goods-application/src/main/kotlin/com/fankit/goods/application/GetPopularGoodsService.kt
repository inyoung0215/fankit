package com.fankit.goods.application

import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.port.inbound.GetPopularGoodsUseCase
import com.fankit.goods.domain.port.outbound.GoodsRankingStore
import com.fankit.goods.domain.port.outbound.GoodsRepository
import org.springframework.stereotype.Service

@Service
class GetPopularGoodsService(
    private val goodsRankingStore: GoodsRankingStore,
    private val goodsRepository: GoodsRepository,
) : GetPopularGoodsUseCase {

    override fun topN(n: Int): List<Goods> {
        require(n in 1..100) { "n은 1~100 사이여야 한다" }

        // Redis Sorted Set ZREVRANGE로 top-N id 즉시 조회 — O(log N + n)
        val topIds = goodsRankingStore.topN(n)

        // 본문은 Mongo에서 — Redis에는 본문 두지 않음 (Cache-Aside의 'index only' 변형)
        return topIds.mapNotNull { goodsRepository.findById(it) }
    }
}
