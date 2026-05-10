package com.fankit.settlement.api

import com.fankit.common.response.ApiResponse
import com.fankit.settlement.api.dto.PaymentRecordRequest
import com.fankit.settlement.api.dto.ReconciliationSummaryResponse
import com.fankit.settlement.api.dto.RunSettlementResponse
import com.fankit.settlement.api.dto.SettlementDetailResponse
import com.fankit.settlement.api.dto.SettlementResponse
import com.fankit.settlement.api.dto.SettlementWithDetailsResponse
import com.fankit.settlement.domain.model.PaymentRecord
import com.fankit.settlement.domain.port.inbound.GetSettlementsUseCase
import com.fankit.settlement.domain.port.inbound.RunReconciliationUseCase
import com.fankit.settlement.domain.port.inbound.RunSettlementUseCase
import com.fankit.settlement.domain.port.outbound.PaymentRecordRepository
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/settlements")
class SettlementController(
    private val runSettlementUseCase: RunSettlementUseCase,
    private val runReconciliationUseCase: RunReconciliationUseCase,
    private val getSettlementsUseCase: GetSettlementsUseCase,
    private val paymentRecordRepository: PaymentRecordRepository,
) {
    // 수동 트리거 (운영 도구용 또는 정합성 재처리)
    @PostMapping("/run")
    fun run(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) period: LocalDate,
    ): ApiResponse<RunSettlementResponse> =
        ApiResponse.success(RunSettlementResponse.from(runSettlementUseCase.run(period)))

    @PostMapping("/reconciliation")
    fun reconcile(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) period: LocalDate,
    ): ApiResponse<ReconciliationSummaryResponse> =
        ApiResponse.success(ReconciliationSummaryResponse.from(runReconciliationUseCase.run(period)))

    @GetMapping("/by-creator/{creatorId}")
    fun byCreator(@PathVariable creatorId: Long): ApiResponse<List<SettlementResponse>> =
        ApiResponse.success(getSettlementsUseCase.byCreator(creatorId).map(SettlementResponse::from))

    @GetMapping("/{id}/details")
    fun details(@PathVariable id: Long): ApiResponse<SettlementWithDetailsResponse> =
        ApiResponse.success(SettlementWithDetailsResponse.from(getSettlementsUseCase.detailsOf(id)))

    // CSV 다운로드 — 크리에이터가 자기 정산 내역 raw data를 받을 수 있음
    @GetMapping("/{id}/csv", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun csv(@PathVariable id: Long): ResponseEntity<String> {
        val w = getSettlementsUseCase.detailsOf(id)
        val csv = buildString {
            appendLine("paymentId,goodsId,salesAmount,commissionAmount,netAmount")
            w.details.forEach { d ->
                appendLine("${d.paymentId},${d.goodsId},${d.salesAmount},${d.commissionAmount},${d.netAmount}")
            }
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, """attachment; filename="settlement-${w.settlement.id}.csv"""")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csv)
    }

    // 시연용: PaymentRecord 직접 INSERT (실전엔 Kafka 컨슈머가 적재)
    // 이 endpoint로 결제 데이터를 채워두면 /run 호출 시 정산 가능
    @PostMapping("/payment-records")
    fun insertPaymentRecord(@RequestBody @Valid req: PaymentRecordRequest): ApiResponse<Unit> {
        paymentRecordRepository.save(
            PaymentRecord(req.paymentId, req.creatorId, req.goodsId, req.amount, req.approvedAt)
        )
        return ApiResponse.success()
    }
}
