package com.fankit.goods.application

import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.port.inbound.RegisterGoodsCommand
import com.fankit.goods.domain.port.outbound.GoodsRepository
import com.fankit.goods.domain.port.outbound.GoodsSearchIndex
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class RegisterGoodsServiceTest {

    private val repository: GoodsRepository = mockk()
    private val searchIndex: GoodsSearchIndex = mockk()
    private val service = RegisterGoodsService(repository, searchIndex)

    @Test
    fun `등록 시 Mongo 저장 후 ES 인덱싱까지 호출되고 id를 반환한다`() {
        every { repository.save(any()) } answers {
            firstArg<Goods>().copy(id = "abc123")
        }
        every { searchIndex.index(any()) } just Runs

        val id = service.register(
            RegisterGoodsCommand(
                creatorId = 1L,
                name = "민호 키링",
                description = "리미티드",
                basePrice = 8000,
                category = Category.KEYRING,
                tags = listOf("민호"),
                options = emptyList(),
                imageUrls = listOf("https://cdn/x.jpg"),
            )
        )

        id shouldBe "abc123"
        verify { repository.save(any()) }
        verify { searchIndex.index(match { it.id == "abc123" }) }
    }
}
