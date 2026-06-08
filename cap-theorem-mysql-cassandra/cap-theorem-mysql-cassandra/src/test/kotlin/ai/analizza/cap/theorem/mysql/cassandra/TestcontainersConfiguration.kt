package ai.analizza.cap.theorem.mysql.cassandra

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> =
        MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("fiapdb")
            .withUsername("fiap")
            .withPassword("fiap123")
}
