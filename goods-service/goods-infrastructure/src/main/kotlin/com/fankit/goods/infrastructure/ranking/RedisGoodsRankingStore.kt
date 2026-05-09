package com.fankit.goods.infrastructure.ranking

import com.fankit.goods.domain.port.outbound.GoodsRankingStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisGoodsRankingStore(
    private val redisTemplate: StringRedisTemplate,
) : GoodsRankingStore {

    // ZINCRBY: O(log N) — score(=조회수) 누적
    override fun increment(goodsId: String, delta: Long) {
        redisTemplate.opsForZSet().incrementScore(KEY, goodsId, delta.toDouble())
    }

    // ZREVRANGE: O(log N + n) — score 내림차순 top-n id
    override fun topN(n: Int): List<String> =
        redisTemplate.opsForZSet().reverseRange(KEY, 0, (n - 1).toLong())?.toList() ?: emptyList()

    companion object {
        // 단일 키에 모든 굿즈 적재 — 단순. 카테고리별 랭킹 필요 시 "goods:ranking:{category}" 분리
        private const val KEY = "goods:ranking"
    }
}
