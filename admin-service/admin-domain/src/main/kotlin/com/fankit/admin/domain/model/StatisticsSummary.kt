package com.fankit.admin.domain.model

data class StatisticsSummary(
    val totalApproved: Long,
    val totalRejected: Long,
    val byType: Map<ApprovalType, TypeCount>,
)

data class TypeCount(
    val approved: Long,
    val rejected: Long,
) {
    val total: Long get() = approved + rejected
}
