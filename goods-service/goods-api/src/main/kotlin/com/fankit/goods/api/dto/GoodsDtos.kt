package com.fankit.goods.api.dto

import com.fankit.goods.domain.model.Category
import com.fankit.goods.domain.model.Goods
import com.fankit.goods.domain.model.GoodsOption
import com.fankit.goods.domain.model.GoodsStatus
import com.fankit.goods.domain.port.inbound.IssueImageUploadUrlCommand
import com.fankit.goods.domain.port.inbound.RegisterGoodsCommand
import com.fankit.goods.domain.port.inbound.SearchGoodsQuery
import com.fankit.goods.domain.port.outbound.PresignedUploadUrl
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class RegisterGoodsRequest(
    @field:Min(1) val creatorId: Long,    // TODO: JWT에서 추출하도록 추후 변경
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:Size(max = 5000) val description: String,
    @field:Min(100) val basePrice: Int,
    val category: Category,
    val tags: List<String> = emptyList(),
    @field:Valid val options: List<GoodsOption> = emptyList(),
    @field:NotEmpty val imageUrls: List<String>,
) {
    fun toCommand() = RegisterGoodsCommand(
        creatorId, name, description, basePrice, category, tags, options, imageUrls,
    )
}

data class RegisterGoodsResponse(val goodsId: String)

data class GoodsResponse(
    val id: String,
    val creatorId: Long,
    val name: String,
    val description: String,
    val basePrice: Int,
    val category: Category,
    val tags: List<String>,
    val options: List<GoodsOption>,
    val imageUrls: List<String>,
    val status: GoodsStatus,
    val viewCount: Long,
) {
    companion object {
        fun from(g: Goods) = GoodsResponse(
            id = g.id ?: error("id 없는 Goods 응답 불가"),
            creatorId = g.creatorId, name = g.name, description = g.description,
            basePrice = g.basePrice, category = g.category, tags = g.tags,
            options = g.options, imageUrls = g.imageUrls, status = g.status,
            viewCount = g.viewCount,
        )
    }
}

data class SearchGoodsRequest(
    val keyword: String? = null,
    val category: Category? = null,
    @field:Min(0) val page: Int = 0,
    @field:Min(1) val size: Int = 20,
) {
    fun toQuery() = SearchGoodsQuery(keyword, category, page, size)
}

data class SearchGoodsResponse(
    val items: List<GoodsResponse>,
    val totalHits: Long,
    val page: Int,
    val size: Int,
)

data class IssueUploadUrlRequest(
    @field:Min(1) val creatorId: Long,
    @field:NotBlank val originalFilename: String,
    @field:NotBlank val contentType: String,
) {
    fun toCommand() = IssueImageUploadUrlCommand(creatorId, originalFilename, contentType)
}

data class UploadUrlResponse(
    val uploadUrl: String,
    val publicUrl: String,
    val expiresInSeconds: Long,
) {
    companion object {
        fun from(u: PresignedUploadUrl) = UploadUrlResponse(u.uploadUrl, u.publicUrl, u.expiresInSeconds)
    }
}
