package com.fankit.goods.application

import com.fankit.goods.domain.exception.GoodsNotFoundException
import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.model.GoodsStatus
import com.fankit.goods.domain.port.outbound.GoodsRankingStore
import com.fankit.goods.domain.port.outbound.GoodsRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class GetGoodsServiceTest {

    private val repository: GoodsRepository = mockk()
    private val ranking: GoodsRankingStore = mockk()
    private val service = GetGoodsService(repository, ranking)

    @Test
    fun `존재하지 않는 굿즈면 GoodsNotFoundException`() {
        every { repository.findById("nope") } returns null
        shouldThrow<GoodsNotFoundException> { service.getById("nope") }
    }

    @Test
    fun `조회 시 Mongo viewCount 증가 + Redis 랭킹 score 증가가 둘 다 호출된다`() {
        val goods = Goods(
            id = "g1", creatorId = 1L, name = "n", description = "d", basePrice = 1000,
            category = Category.OTHER, tags = emptyList(), options = emptyList(),
            imageUrls = listOf("u"), status = GoodsStatus.APPROVED,
        )
        every { repository.findById("g1") } returns goods
        every { repository.incrementViewCount("g1") } just Runs
        every { ranking.increment("g1", 1) } just Runs

        val result = service.getById("g1")

        result.id shouldBe "g1"
        verify { repository.incrementViewCount("g1") }
        verify { ranking.increment("g1", 1) }
    }
}
