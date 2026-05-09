package com.fankit.goods.domain.port.inbound

import com.fankit.goods.domain.port.outbound.PresignedUploadUrl

interface IssueImageUploadUrlUseCase {
    fun issue(command: IssueImageUploadUrlCommand): PresignedUploadUrl
}

data class IssueImageUploadUrlCommand(
    val creatorId: Long,
    val originalFilename: String,
    val contentType: String,
)
