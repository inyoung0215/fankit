package com.fankit.settlement.infrastructure.quartz

import com.fankit.settlement.domain.port.inbound.RunSettlementUseCase
import org.quartz.CronScheduleBuilder
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.quartz.spi.TriggerFiredBundle

// SpringBeanJobFactory + AutowireCapableBeanFactory:
//   Quartz가 만든 Job 인스턴스에 Spring 빈을 주입할 수 있게 해줌
//   (안 쓰면 RunSettlementUseCase가 lateinit인 채로 NPE)
@Configuration
class QuartzConfig {

    @Bean
    fun settlementJobDetail(): JobDetailFactoryBean {
        val factory = JobDetailFactoryBean()
        factory.setJobClass(SettlementQuartzJob::class.java)
        factory.setName("settlementQuartzJob")
        factory.setDurability(true)   // 트리거 없어도 등록 유지 (수동 트리거 가능)
        return factory
    }

    @Bean
    fun settlementCronTrigger(
        settlementJobDetail: JobDetail,
        @Value("\${settlement.schedule.cron:0 0 2 * * ?}") cron: String,
    ): Trigger = TriggerBuilder.newTrigger()
        .forJob(settlementJobDetail)
        .withIdentity("settlementCronTrigger")
        .withSchedule(
            CronScheduleBuilder.cronSchedule(cron)
                .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"))
        )
        .build()

    @Bean
    fun springBeanJobFactory(beanFactory: AutowireCapableBeanFactory): SpringBeanJobFactory =
        object : SpringBeanJobFactory() {
            override fun createJobInstance(bundle: TriggerFiredBundle): Any {
                val job = super.createJobInstance(bundle)
                beanFactory.autowireBean(job)   // RunSettlementUseCase 주입
                return job
            }
        }

    // SchedulerFactoryBean을 Spring Boot 자동 설정과 연동하기 위한 customizer
    @Bean
    fun schedulerFactoryBeanCustomizer(jobFactory: SpringBeanJobFactory) =
        org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer { factory: SchedulerFactoryBean ->
            factory.setJobFactory(jobFactory)
        }
}
