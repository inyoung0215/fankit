package com.fankit.admin.api

import com.fankit.admin.api.dto.ApproveRequest
import com.fankit.admin.api.dto.DecisionResponse
import com.fankit.admin.api.dto.RejectRequest
import com.fankit.admin.api.dto.StatisticsResponse
import com.fankit.admin.domain.model.ApprovalType
import com.fankit.admin.domain.port.inbound.ApproveGoodsUseCase
import com.fankit.admin.domain.port.inbound.GetStatisticsUseCase
import com.fankit.admin.domain.port.inbound.ListDecisionsUseCase
import com.fankit.admin.domain.port.inbound.PromoteCreatorUseCase
import com.fankit.admin.domain.port.inbound.RejectGoodsUseCase
import com.fankit.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val approveGoodsUseCase: ApproveGoodsUseCase,
    private val rejectGoodsUseCase: RejectGoodsUseCase,
    private val promoteCreatorUseCase: PromoteCreatorUseCase,
    private val listDecisionsUseCase: ListDecisionsUseCase,
    private val getStatisticsUseCase: GetStatisticsUseCase,
) {
    @PostMapping("/goods/{goodsId}/approve")
    fun approveGoods(
        @PathVariable goodsId: String,
        @RequestBody @Valid request: ApproveRequest,
    ): ApiResponse<DecisionResponse> =
        ApiResponse.success(DecisionResponse.from(approveGoodsUseCase.approve(goodsId, request.adminId)))

    @PostMapping("/goods/{goodsId}/reject")
    fun rejectGoods(
        @PathVariable goodsId: String,
        @RequestBody @Valid request: RejectRequest,
    ): ApiResponse<DecisionResponse> =
        ApiResponse.success(
            DecisionResponse.from(rejectGoodsUseCase.reject(goodsId, request.adminId, request.reason))
        )

    @PostMapping("/users/{userId}/promote-to-creator")
    fun promoteCreator(
        @PathVariable userId: Long,
        @RequestBody @Valid request: ApproveRequest,
    ): ApiResponse<DecisionResponse> =
        ApiResponse.success(DecisionResponse.from(promoteCreatorUseCase.promote(userId, request.adminId)))

    @GetMapping("/decisions")
    fun list(
        @RequestParam(required = false) type: ApprovalType?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<List<DecisionResponse>> =
        ApiResponse.success(listDecisionsUseCase.list(type, page, size).map(DecisionResponse::from))

    @GetMapping("/statistics")
    fun statistics(): ApiResponse<StatisticsResponse> =
        ApiResponse.success(StatisticsResponse.from(getStatisticsUseCase.summary()))
}
