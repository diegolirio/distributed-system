package br.com.ralvin.propertyanalysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class PropertyAnalysisApplicationTests {

	@Test
	void contextLoads() {
	}

}
