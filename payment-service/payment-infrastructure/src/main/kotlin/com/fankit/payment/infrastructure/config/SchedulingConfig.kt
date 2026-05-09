package com.fankit.payment.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

// OutboxRelayScheduler의 @Scheduled 활성화
@Configuration
@EnableScheduling
class SchedulingConfig
