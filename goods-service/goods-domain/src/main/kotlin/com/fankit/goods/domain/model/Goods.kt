package com.fankit.goods.domain.model

import com.fankit.goods.domain.exception.IllegalGoodsStateTransitionException
import java.time.Instant

// Goods aggregate root
//
// id: MongoDB ObjectId 문자열 — RDB의 auto-increment Long 대비 분산 환경에서 충돌 없이 생성 가능
// viewCount: 조회수 (인기 굿즈 랭킹 산출의 입력값) — Redis Sorted Set과 별개로 영구 보관
data class Goods(
    val id: String? = null,
    val creatorId: Long,
    val name: String,
    val description: String,
    val basePrice: Int,
    val category: Category,
    val tags: List<String>,
    val options: List<GoodsOption>,
    val imageUrls: List<String>,
    val status: GoodsStatus,
    val viewCount: Long = 0,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    fun approve(): Goods {
        check(status == GoodsStatus.PENDING_APPROVAL) {
            throw IllegalGoodsStateTransitionException(status, GoodsStatus.APPROVED)
        }
        return copy(status = GoodsStatus.APPROVED)
    }

    fun reject(): Goods {
        check(status == GoodsStatus.PENDING_APPROVAL) {
            throw IllegalGoodsStateTransitionException(status, GoodsStatus.REJECTED)
        }
        return copy(status = GoodsStatus.REJECTED)
    }

    fun markSoldOut(): Goods {
        check(status == GoodsStatus.APPROVED) {
            throw IllegalGoodsStateTransitionException(status, GoodsStatus.SOLD_OUT)
        }
        return copy(status = GoodsStatus.SOLD_OUT)
    }

    fun discontinue(): Goods {
        check(status == GoodsStatus.APPROVED || status == GoodsStatus.SOLD_OUT) {
            throw IllegalGoodsStateTransitionException(status, GoodsStatus.DISCONTINUED)
        }
        return copy(status = GoodsStatus.DISCONTINUED)
    }

    val isVisibleToBuyers: Boolean
        get() = status == GoodsStatus.APPROVED || status == GoodsStatus.SOLD_OUT

    companion object {
        fun create(
            creatorId: Long,
            name: String,
            description: String,
            basePrice: Int,
            category: Category,
            tags: List<String>,
            options: List<GoodsOption>,
            imageUrls: List<String>,
        ): Goods {
            require(name.isNotBlank() && name.length <= 100) { "굿즈명은 1~100자여야 한다" }
            require(description.length <= 5000) { "상세설명은 5000자 이하여야 한다" }
            require(basePrice in 100..10_000_000) { "기본가는 100원~1000만원 범위여야 한다" }
            require(tags.size <= 20) { "태그는 최대 20개까지 가능하다" }
            require(imageUrls.isNotEmpty()) { "굿즈는 이미지 1장 이상 필수" }
            require(imageUrls.size <= 10) { "이미지는 최대 10장까지 가능하다" }
            return Goods(
                creatorId = creatorId,
                name = name,
                description = description,
                basePrice = basePrice,
                category = category,
                tags = tags,
                options = options,
                imageUrls = imageUrls,
                status = GoodsStatus.PENDING_APPROVAL,   // 등록 직후엔 항상 승인 대기
            )
        }
    }
}
