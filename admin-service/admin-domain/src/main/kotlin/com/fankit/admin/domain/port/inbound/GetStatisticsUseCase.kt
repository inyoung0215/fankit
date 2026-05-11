package com.fankit.admin.domain.port.inbound

import com.fankit.admin.domain.model.StatisticsSummary

interface GetStatisticsUseCase {
    fun summary(): StatisticsSummary
}
