package com.fankit.goods.domain

import com.fankit.goods.domain.exception.IllegalGoodsStateTransitionException
import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.model.GoodsOption
import com.fankit.goods.domain.model.GoodsStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GoodsTest {

    private fun pendingGoods(): Goods = Goods.create(
        creatorId = 1L,
        name = "민호 포토카드",
        description = "한정판",
        basePrice = 5000,
        category = Category.PHOTO_CARD,
        tags = listOf("민호", "한정판"),
        options = listOf(
            GoodsOption("멤버", listOf("민호", "지훈"))
        ),
        imageUrls = listOf("https://cdn.example.com/img1.jpg"),
    )

    @Test
    fun `create는 항상 PENDING_APPROVAL 상태로 시작한다`() {
        pendingGoods().status shouldBe GoodsStatus.PENDING_APPROVAL
    }

    @Test
    fun `이미지가 없으면 등록 불가`() {
        shouldThrow<IllegalArgumentException> {
            Goods.create(
                creatorId = 1L, name = "x", description = "x", basePrice = 1000,
                category = Category.OTHER, tags = emptyList(), options = emptyList(),
                imageUrls = emptyList(),
            )
        }
    }

    @Test
    fun `PENDING은 approve 가능`() {
        pendingGoods().approve().status shouldBe GoodsStatus.APPROVED
    }

    @Test
    fun `APPROVED 상태에서 approve 재호출은 상태 전이 예외`() {
        val approved = pendingGoods().approve()
        shouldThrow<IllegalGoodsStateTransitionException> { approved.approve() }
    }

    @Test
    fun `APPROVED만 markSoldOut 가능 - PENDING에서 호출 시 예외`() {
        shouldThrow<IllegalGoodsStateTransitionException> { pendingGoods().markSoldOut() }
    }

    @Test
    fun `APPROVED와 SOLD_OUT은 discontinue 가능`() {
        pendingGoods().approve().discontinue().status shouldBe GoodsStatus.DISCONTINUED
        pendingGoods().approve().markSoldOut().discontinue().status shouldBe GoodsStatus.DISCONTINUED
    }

    @Test
    fun `구매자 노출 가시성 - APPROVED와 SOLD_OUT만 true`() {
        pendingGoods().isVisibleToBuyers shouldBe false
        pendingGoods().approve().isVisibleToBuyers shouldBe true
        pendingGoods().approve().markSoldOut().isVisibleToBuyers shouldBe true
        pendingGoods().approve().discontinue().isVisibleToBuyers shouldBe false
    }
}
