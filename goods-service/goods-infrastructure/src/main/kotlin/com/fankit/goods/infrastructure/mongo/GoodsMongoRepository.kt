package com.fankit.goods.infrastructure.mongo

import org.springframework.data.mongodb.repository.MongoRepository

interface GoodsMongoRepository : MongoRepository<GoodsMongoDocument, String>
