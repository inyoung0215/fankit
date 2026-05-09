package com.fankit.goods.infrastructure.search

import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.port.outbound.GoodsSearchIndex
import com.fankit.goods.domain.port.outbound.SearchQuery
import com.fankit.goods.domain.port.outbound.SearchResult
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Component

@Component
class ElasticsearchGoodsSearchIndexAdapter(
    private val operations: ElasticsearchOperations,
) : GoodsSearchIndex {

    override fun index(goods: Goods) {
        operations.save(GoodsSearchDocument.fromDomain(goods))
    }

    override fun delete(id: String) {
        operations.delete(id, GoodsSearchDocument::class.java)
    }

    override fun search(query: SearchQuery): SearchResult {
        // Elasticsearch Java client의 query DSL 빌드
        // 키워드: name OR description (Nori 형태소 분석 적용)
        // 카테고리: keyword 정확 매치
        val nativeQuery = NativeQuery.builder()
            .withQuery { q ->
                q.bool { b ->
                    if (!query.keyword.isNullOrBlank()) {
                        b.must { m ->
                            m.multiMatch { mm ->
                                mm.query(query.keyword).fields("name", "description")
                            }
                        }
                    }
                    query.category?.let {
                        b.filter { f -> f.term { t -> t.field("category").value(it.name) } }
                    }
                    // status가 PENDING_APPROVAL/REJECTED인 것은 검색 노출 X
                    b.filter { f ->
                        f.terms { t ->
                            t.field("status").terms { v ->
                                v.value(
                                    listOf("APPROVED", "SOLD_OUT")
                                        .map { co.elastic.clients.elasticsearch._types.FieldValue.of(it) }
                                )
                            }
                        }
                    }
                    b
                }
            }
            .withPageable(PageRequest.of(query.page, query.size))
            .build()

        val hits = operations.search(nativeQuery, GoodsSearchDocument::class.java)
        return SearchResult(
            ids = hits.searchHits.map { it.content.id },
            totalHits = hits.totalHits,
        )
    }
}
