package com.fankit.goods

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class GoodsServiceApplication

fun main(args: Array<String>) {
    runApplication<GoodsServiceApplication>(*args)
}
