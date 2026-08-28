package br.com.ralvin.propertyanalysis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "gemini.api-key=test-key-not-used")
class PropertyAnalysisApplicationTests {

	@Test
	void contextLoads() {
	}

}
