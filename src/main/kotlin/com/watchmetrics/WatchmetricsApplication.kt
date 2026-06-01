package com.watchmetrics

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WatchmetricsApplication

fun main(args: Array<String>) {
    runApplication<WatchmetricsApplication>(*args)
}
