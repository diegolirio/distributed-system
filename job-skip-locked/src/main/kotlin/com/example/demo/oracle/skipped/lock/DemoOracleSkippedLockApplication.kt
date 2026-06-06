package com.example.demo.oracle.skipped.lock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class DemoOracleSkippedLockApplication

fun main(args: Array<String>) {
	runApplication<DemoOracleSkippedLockApplication>(*args)
}
