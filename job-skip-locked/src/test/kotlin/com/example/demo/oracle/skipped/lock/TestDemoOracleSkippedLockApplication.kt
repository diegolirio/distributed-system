package com.example.demo.oracle.skipped.lock

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<DemoOracleSkippedLockApplication>().with(TestcontainersConfiguration::class).run(*args)
}
