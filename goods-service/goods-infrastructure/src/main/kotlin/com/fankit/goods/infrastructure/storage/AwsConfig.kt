package com.fankit.goods.infrastructure.storage

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
@EnableConfigurationProperties(S3Properties::class)
class AwsConfig {

    // 자격 증명은 default credentials provider chain 사용
    //   순서: env vars → ~/.aws/credentials → IAM role
    // 운영(EC2/EKS)에선 IAM role, 로컬은 ~/.aws/credentials
    @Bean
    fun s3Presigner(props: S3Properties): S3Presigner =
        S3Presigner.builder()
            .region(Region.of(props.region))
            .build()
}
