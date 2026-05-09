package com.fankit.goods.application

import com.fankit.goods.domain.exception.GoodsNotFoundException
import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.port.inbound.GetGoodsUseCase
import com.fankit.goods.domain.port.outbound.GoodsRankingStore
import com.fankit.goods.domain.port.outbound.GoodsRepository
import org.springframework.stereotype.Service

@Service
class GetGoodsService(
    private val goodsRepository: GoodsRepository,
    private val goodsRankingStore: GoodsRankingStore,
) : GetGoodsUseCase {

    override fun getById(id: String): Goods {
        val goods = goodsRepository.findById(id) ?: throw GoodsNotFoundException(id)

        // 조회수 이중 갱신:
        //   - Mongo.viewCount: 영구 보관 (정산/통계용)
        //   - Redis Sorted Set: 인기 굿즈 랭킹 (top-N 즉시 조회용)
        // 트랜잭션 묶지 않음 — 조회수는 ±1 정확도가 critical하지 않음 (eventual)
        goodsRepository.incrementViewCount(id)
        goodsRankingStore.increment(id)

        return goods
    }
}
