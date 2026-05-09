package com.fankit.goods.domain.port.outbound

import java.time.Duration

// 이미지 저장소 (S3) 추상화
//
// Pre-signed URL 패턴:
//   1) 클라이언트가 서버에 "업로드할 객체 키" 요청
//   2) 서버가 S3에 임시 권한이 부여된 PUT URL 발급 (예: 5분 유효)
//   3) 클라이언트가 이 URL로 S3에 직접 PUT — 서버를 거치지 않음
//
// 이점: 서버 트래픽/메모리/디스크 부담 없음. 대용량 파일도 OK.
//      서버는 자격증명만 잠깐 빌려주는 역할.
interface ImageStorage {
    fun issuePresignedUploadUrl(objectKey: String, ttl: Duration): PresignedUploadUrl
    fun publicUrl(objectKey: String): String
}

data class PresignedUploadUrl(
    val uploadUrl: String,
    val publicUrl: String,
    val expiresInSeconds: Long,
)
