package com.fankit.goods.domain.port.outbound

// Redis Sorted Set 기반 인기 굿즈 랭킹
//
// 왜 Redis Sorted Set?
//   - DB COUNT(*) ORDER BY view_count DESC LIMIT N은 매 호출마다 풀스캔 → O(N log N)
//   - Sorted Set은 ZADD/ZINCRBY가 O(log N), ZREVRANGE가 O(log N + M) — top-N 조회 즉시
//   - TTL 5분 (Cache-Aside) — 영구 데이터는 MongoDB.viewCount, Redis는 hot data 캐시
//
// score = 조회수 (또는 시간 가중 점수). 단순화 위해 누적 조회수 사용.
interface GoodsRankingStore {
    fun increment(goodsId: String, delta: Long = 1)
    fun topN(n: Int): List<String>   // 점수 내림차순 굿즈 id
}
