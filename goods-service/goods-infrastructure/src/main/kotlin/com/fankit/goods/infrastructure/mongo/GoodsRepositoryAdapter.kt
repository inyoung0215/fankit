package com.fankit.goods.infrastructure.mongo

import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.port.outbound.GoodsRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class GoodsRepositoryAdapter(
    private val mongoRepository: GoodsMongoRepository,
    private val mongoTemplate: MongoTemplate,
) : GoodsRepository {

    override fun save(goods: Goods): Goods =
        mongoRepository.save(GoodsMongoDocument.fromDomain(goods)).toDomain()

    override fun findById(id: String): Goods? =
        mongoRepository.findById(id).orElse(null)?.toDomain()

    override fun deleteById(id: String) {
        mongoRepository.deleteById(id)
    }

    // viewCount는 atomic 증가 필요 — full save로 race condition 발생 시 ±count 누락
    // MongoDB의 $inc 연산자로 단일 atomic operation
    override fun incrementViewCount(id: String) {
        mongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").`is`(id)),
            Update().inc("viewCount", 1),
            GoodsMongoDocument::class.java,
        )
    }
}
