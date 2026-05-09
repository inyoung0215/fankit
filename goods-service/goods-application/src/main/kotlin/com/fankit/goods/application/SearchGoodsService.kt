package com.fankit.goods.application

import com.fankit.goods.domain.port.inbound.SearchGoodsQuery
import com.fankit.goods.domain.port.inbound.SearchGoodsResult
import com.fankit.goods.domain.port.inbound.SearchGoodsUseCase
import com.fankit.goods.domain.port.outbound.GoodsRepository
import com.fankit.goods.domain.port.outbound.GoodsSearchIndex
import com.fankit.goods.domain.port.outbound.SearchQuery
import org.springframework.stereotype.Service

@Service
class SearchGoodsService(
    private val goodsSearchIndex: GoodsSearchIndex,
    private val goodsRepository: GoodsRepository,
) : SearchGoodsUseCase {

    override fun search(query: SearchGoodsQuery): SearchGoodsResult {
        // ① ES에서 ID 페이지네이션 결과만 가져옴 (정렬·필터링은 ES 책임)
        val esResult = goodsSearchIndex.search(
            SearchQuery(query.keyword, query.category, query.page, query.size)
        )

        // ② Mongo에서 ID로 본문 fetch — ES는 인덱스만, 본문 SoT는 Mongo
        // (ES에 본문 다 넣지 않는 이유: 본문 변경 시 ES 재색인 비용 → 인덱스 슬림화)
        val items = esResult.ids.mapNotNull { goodsRepository.findById(it) }

        return SearchGoodsResult(
            items = items,
            totalHits = esResult.totalHits,
            page = query.page,
            size = query.size,
        )
    }
}
