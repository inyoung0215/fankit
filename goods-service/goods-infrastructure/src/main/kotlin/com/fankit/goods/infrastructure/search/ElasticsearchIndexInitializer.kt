package com.fankit.goods.infrastructure.search

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Component

// 앱 시작 시 ES 인덱스가 없으면 settings + mapping JSON으로 생성
// (createIndex=false로 두고 명시적으로 만드는 이유: settings/mappings JSON 적용 확실히 하기 위해)
@Component
class ElasticsearchIndexInitializer(
    private val operations: ElasticsearchOperations,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val indexOps = operations.indexOps(GoodsSearchDocument::class.java)
        if (!indexOps.exists()) {
            indexOps.createWithMapping()
        }
    }
}
