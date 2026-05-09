package com.fankit.goods.infrastructure.mongo

import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.model.GoodsOption
import com.fankit.goods.domain.model.GoodsStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "goods")
class GoodsMongoDocument(
    @Id
    var id: String? = null,

    @Indexed
    var creatorId: Long,

    var name: String,
    var description: String,
    var basePrice: Int,

    @Indexed
    var category: Category,

    @Indexed
    var tags: List<String>,

    var options: List<GoodsOption>,
    var imageUrls: List<String>,

    @Indexed
    var status: GoodsStatus,

    var viewCount: Long = 0,

    @CreatedDate
    var createdAt: Instant? = null,

    @LastModifiedDate
    var updatedAt: Instant? = null,
) {
    fun toDomain(): Goods = Goods(
        id = id, creatorId = creatorId, name = name, description = description,
        basePrice = basePrice, category = category, tags = tags, options = options,
        imageUrls = imageUrls, status = status, viewCount = viewCount,
        createdAt = createdAt, updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(g: Goods): GoodsMongoDocument = GoodsMongoDocument(
            id = g.id, creatorId = g.creatorId, name = g.name, description = g.description,
            basePrice = g.basePrice, category = g.category, tags = g.tags, options = g.options,
            imageUrls = g.imageUrls, status = g.status, viewCount = g.viewCount,
            createdAt = g.createdAt, updatedAt = g.updatedAt,
        )
    }
}
