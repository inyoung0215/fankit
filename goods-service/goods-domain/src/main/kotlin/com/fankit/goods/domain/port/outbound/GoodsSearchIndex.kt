package com.fankit.goods.domain.port.outbound

import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.Goods

// Elasticsearch + Nori 검색 추상화
//
// 왜 ES인가?
//   MongoDB의 $text는 한국어 형태소 분석을 지원하지 않음 → "민호 키링"을 정확한 매치로만 찾음
//   ES + Nori plugin은 한국어 형태소 분석으로 "민호키링" "민호의 키링" 모두 매치 가능
//
// 일관성: MongoDB가 SoT(Source of Truth), ES는 검색 인덱스.
//        save() 시 양쪽 동시 갱신 (eventual consistency 허용 — 검색은 약간 늦게 반영돼도 OK)
interface GoodsSearchIndex {
    fun index(goods: Goods)
    fun delete(id: String)
    fun search(query: SearchQuery): SearchResult
}

data class SearchQuery(
    val keyword: String? = null,
    val category: Category? = null,
    val page: Int = 0,
    val size: Int = 20,
) {
    init {
        require(page >= 0) { "page는 0 이상이어야 한다" }
        require(size in 1..100) { "size는 1~100 사이여야 한다" }
    }
}

data class SearchResult(
    val ids: List<String>,
    val totalHits: Long,
)
