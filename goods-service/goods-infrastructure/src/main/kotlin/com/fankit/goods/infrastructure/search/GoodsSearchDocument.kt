package com.fankit.goods.infrastructure.search

import com.fankit.goods.domain.model.Goods
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.Mapping
import org.springframework.data.elasticsearch.annotations.Setting

// ES 인덱스 = "goods" (Mongo와 동일 이름이지만 다른 저장소).
// settings/mappings는 JSON 리소스로 분리 — Nori 분석기 등 복잡한 설정 가독성 ↑
@Document(indexName = "goods", createIndex = false)   // 인덱스 생성은 IndexInitializer가 담당
@Setting(settingPath = "elasticsearch/goods-settings.json")
@Mapping(mappingPath = "elasticsearch/goods-mapping.json")
data class GoodsSearchDocument(
    @Id
    val id: String,

    @Field(type = FieldType.Text, analyzer = "korean")
    val name: String,

    @Field(type = FieldType.Text, analyzer = "korean")
    val description: String,

    @Field(type = FieldType.Keyword)
    val tags: List<String>,

    @Field(type = FieldType.Keyword)
    val category: String,

    @Field(type = FieldType.Long)
    val creatorId: Long,

    @Field(type = FieldType.Keyword)
    val status: String,
) {
    companion object {
        fun fromDomain(g: Goods): GoodsSearchDocument = GoodsSearchDocument(
            id = g.id ?: error("id 없는 Goods는 인덱싱 불가"),
            name = g.name,
            description = g.description,
            tags = g.tags,
            category = g.category.name,
            creatorId = g.creatorId,
            status = g.status.name,
        )
    }
}
