package ai.analizza.system.async.kafka;

import org.springframework.boot.SpringApplication;

public class TestSystemAsyncKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.from(SystemAsyncKafkaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
