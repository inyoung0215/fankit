package com.fankit.goods.infrastructure.storage

import com.fankit.goods.domain.port.outbound.ImageStorage
import com.fankit.goods.domain.port.outbound.PresignedUploadUrl
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class S3ImageStorage(
    private val s3Presigner: S3Presigner,
    private val s3Properties: S3Properties,
) : ImageStorage {

    override fun issuePresignedUploadUrl(objectKey: String, ttl: Duration): PresignedUploadUrl {
        val putRequest = PutObjectRequest.builder()
            .bucket(s3Properties.bucket)
            .key(objectKey)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .putObjectRequest(putRequest)
            .build()

        val presigned = s3Presigner.presignPutObject(presignRequest)

        return PresignedUploadUrl(
            uploadUrl = presigned.url().toString(),
            publicUrl = publicUrl(objectKey),
            expiresInSeconds = ttl.seconds,
        )
    }

    // CloudFront 도입 전까진 S3 virtual-hosted style URL 직접 사용
    override fun publicUrl(objectKey: String): String =
        "https://${s3Properties.bucket}.s3.${s3Properties.region}.amazonaws.com/$objectKey"
}
