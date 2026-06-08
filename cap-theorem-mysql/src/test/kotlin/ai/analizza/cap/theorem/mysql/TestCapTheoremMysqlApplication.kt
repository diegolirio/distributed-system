package ai.analizza.cap.theorem.mysql

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<CapTheoremMysqlApplication>().with(TestcontainersConfiguration::class).run(*args)
}
