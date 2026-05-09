package com.fankit.goods.application

import com.fankit.goods.domain.port.inbound.IssueImageUploadUrlCommand
import com.fankit.goods.domain.port.inbound.IssueImageUploadUrlUseCase
import com.fankit.goods.domain.port.outbound.ImageStorage
import com.fankit.goods.domain.port.outbound.PresignedUploadUrl
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.util.UUID

@Service
class IssueImageUploadUrlService(
    private val imageStorage: ImageStorage,
) : IssueImageUploadUrlUseCase {

    override fun issue(command: IssueImageUploadUrlCommand): PresignedUploadUrl {
        validate(command)

        // 키 패턴: goods/{yyyy/MM/dd}/{creatorId}/{uuid}.{ext}
        // 이점: 날짜 기반 디렉토리 — S3 prefix listing 효율, 라이프사이클 정책 적용 쉬움
        //      uuid — 충돌 없음, 원본 파일명 노출 안함 (보안)
        val today = LocalDate.now()
        val ext = command.originalFilename.substringAfterLast('.', "bin")
        val objectKey = "goods/${today.year}/${"%02d".format(today.monthValue)}/" +
            "${"%02d".format(today.dayOfMonth)}/${command.creatorId}/${UUID.randomUUID()}.$ext"

        return imageStorage.issuePresignedUploadUrl(objectKey, ttl = PRESIGN_TTL)
    }

    private fun validate(command: IssueImageUploadUrlCommand) {
        require(command.contentType in ALLOWED_CONTENT_TYPES) {
            "허용되지 않는 이미지 형식: ${command.contentType}"
        }
    }

    companion object {
        private val PRESIGN_TTL = Duration.ofMinutes(5)
        private val ALLOWED_CONTENT_TYPES = setOf(
            "image/jpeg", "image/png", "image/webp", "image/gif",
        )
    }
}
