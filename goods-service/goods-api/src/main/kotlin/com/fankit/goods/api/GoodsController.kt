package com.fankit.goods.api

import com.fankit.common.response.ApiResponse
import com.fankit.goods.api.dto.GoodsResponse
import com.fankit.goods.api.dto.IssueUploadUrlRequest
import com.fankit.goods.api.dto.RegisterGoodsRequest
import com.fankit.goods.api.dto.RegisterGoodsResponse
import com.fankit.goods.api.dto.SearchGoodsRequest
import com.fankit.goods.api.dto.SearchGoodsResponse
import com.fankit.goods.api.dto.UploadUrlResponse
import com.fankit.goods.domain.port.inbound.GetGoodsUseCase
import com.fankit.goods.domain.port.inbound.GetPopularGoodsUseCase
import com.fankit.goods.domain.port.inbound.IssueImageUploadUrlUseCase
import com.fankit.goods.domain.port.inbound.RegisterGoodsUseCase
import com.fankit.goods.domain.port.inbound.SearchGoodsUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/goods")
class GoodsController(
    private val registerGoodsUseCase: RegisterGoodsUseCase,
    private val getGoodsUseCase: GetGoodsUseCase,
    private val searchGoodsUseCase: SearchGoodsUseCase,
    private val getPopularGoodsUseCase: GetPopularGoodsUseCase,
    private val issueImageUploadUrlUseCase: IssueImageUploadUrlUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody @Valid request: RegisterGoodsRequest): ApiResponse<RegisterGoodsResponse> {
        val id = registerGoodsUseCase.register(request.toCommand())
        return ApiResponse.success(RegisterGoodsResponse(id))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApiResponse<GoodsResponse> =
        ApiResponse.success(GoodsResponse.from(getGoodsUseCase.getById(id)))

    @GetMapping("/search")
    fun search(@ModelAttribute @Valid request: SearchGoodsRequest): ApiResponse<SearchGoodsResponse> {
        val result = searchGoodsUseCase.search(request.toQuery())
        return ApiResponse.success(
            SearchGoodsResponse(
                items = result.items.map(GoodsResponse::from),
                totalHits = result.totalHits,
                page = result.page,
                size = result.size,
            )
        )
    }

    @GetMapping("/popular")
    fun popular(@RequestParam(defaultValue = "10") n: Int): ApiResponse<List<GoodsResponse>> =
        ApiResponse.success(getPopularGoodsUseCase.topN(n).map(GoodsResponse::from))

    @PostMapping("/upload-url")
    fun issueUploadUrl(@RequestBody @Valid request: IssueUploadUrlRequest): ApiResponse<UploadUrlResponse> =
        ApiResponse.success(UploadUrlResponse.from(issueImageUploadUrlUseCase.issue(request.toCommand())))
}
