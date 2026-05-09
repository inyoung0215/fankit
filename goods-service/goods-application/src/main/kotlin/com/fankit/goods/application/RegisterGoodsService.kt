package com.fankit.goods.application

import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.port.inbound.RegisterGoodsCommand
import com.fankit.goods.domain.port.inbound.RegisterGoodsUseCase
import com.fankit.goods.domain.port.outbound.GoodsRepository
import com.fankit.goods.domain.port.outbound.GoodsSearchIndex
import org.springframework.stereotype.Service

@Service
class RegisterGoodsService(
    private val goodsRepository: GoodsRepository,
    private val goodsSearchIndex: GoodsSearchIndex,
) : RegisterGoodsUseCase {

    override fun register(command: RegisterGoodsCommand): String {
        val goods = Goods.create(
            creatorId = command.creatorId,
            name = command.name,
            description = command.description,
            basePrice = command.basePrice,
            category = command.category,
            tags = command.tags,
            options = command.options,
            imageUrls = command.imageUrls,
        )

        // ① MongoDB가 SoT — 영속화 먼저
        val saved = goodsRepository.save(goods)

        // ② ES 인덱싱 — eventual consistency 허용
        // (ES indexing 실패해도 굿즈 등록 자체는 성공해야 함 — 추후 재인덱싱 배치로 복구)
        // 실전에선 try/catch로 swallow 후 retry queue에 적재. 여기선 단순화.
        goodsSearchIndex.index(saved)

        return saved.id ?: error("저장된 Goods에 id가 없다 — Repository 어댑터 버그")
    }
}
