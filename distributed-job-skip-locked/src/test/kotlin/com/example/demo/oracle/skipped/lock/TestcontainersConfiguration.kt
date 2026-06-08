package com.example.demo.oracle.skipped.lock

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.oracle.OracleContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	fun oracleFreeContainer(): OracleContainer {
		return OracleContainer(DockerImageName.parse("gvenzl/oracle-free:latest"))
			.withStartupTimeout(Duration.ofMinutes(5))
	}

}
