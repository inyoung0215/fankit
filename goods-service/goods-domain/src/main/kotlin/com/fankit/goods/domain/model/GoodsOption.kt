package com.fankit.goods.domain.model

// 굿즈 옵션 — 옵션 종류별로 값과 추가금이 다름
// 예) 의류 사이즈: name="사이즈", values=["S","M","L","XL"], additionalPrices={"XL": 2000}
//     포토카드 멤버: name="멤버", values=["멤버A","멤버B"], additionalPrices={}
//
// MongoDB 선택 이유: 굿즈 종류별로 옵션 스키마가 너무 다양 — RDB의 정형 스키마로 표현 비효율
data class GoodsOption(
    val name: String,
    val values: List<String>,
    val additionalPrices: Map<String, Int> = emptyMap(),
) {
    init {
        require(name.isNotBlank()) { "옵션 이름은 비어있을 수 없다" }
        require(values.isNotEmpty()) { "옵션 값은 최소 1개 이상이어야 한다" }
        require(additionalPrices.keys.all { it in values }) {
            "additionalPrices의 key는 모두 values에 포함되어야 한다"
        }
        require(additionalPrices.values.all { it >= 0 }) { "추가금은 0 이상이어야 한다" }
    }
}
