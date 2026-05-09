package com.fankit.goods.application

import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.model.GoodsStatus
import com.fankit.goods.domain.port.outbound.GoodsRankingStore
import com.fankit.goods.domain.port.outbound.GoodsRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class GetPopularGoodsServiceTest {

    private val ranking: GoodsRankingStore = mockk()
    private val repository: GoodsRepository = mockk()
    private val service = GetPopularGoodsService(ranking, repository)

    private fun goodsOf(id: String) = Goods(
        id = id, creatorId = 1L, name = id, description = "x", basePrice = 1000,
        category = Category.OTHER, tags = emptyList(), options = emptyList(),
        imageUrls = listOf("u"), status = GoodsStatus.APPROVED,
    )

    @Test
    fun `topN은 Redis 순서대로 Mongo에서 본문을 fetch한다`() {
        every { ranking.topN(3) } returns listOf("a", "b", "c")
        every { repository.findById("a") } returns goodsOf("a")
        every { repository.findById("b") } returns goodsOf("b")
        every { repository.findById("c") } returns goodsOf("c")

        val result = service.topN(3)

        result.map { it.id } shouldBe listOf("a", "b", "c")
    }

    @Test
    fun `Mongo에 없는 id는 결과에서 제외된다 - 정합성 일시 깨짐 허용`() {
        every { ranking.topN(2) } returns listOf("a", "ghost")
        every { repository.findById("a") } returns goodsOf("a")
        every { repository.findById("ghost") } returns null

        val result = service.topN(2)

        result.map { it.id } shouldBe listOf("a")
    }

    @Test
    fun `n 범위 검증`() {
        shouldThrow<IllegalArgumentException> { service.topN(0) }
        shouldThrow<IllegalArgumentException> { service.topN(101) }
    }
}
