package br.com.ralvin.propertyanalysis.analysis;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class LangChain4jPropertiesAnalysisIntegrationTest {

	private static LangChain4jPropertiesAnalysis analysis;
	private static String pageContent;

	@BeforeAll
	static void setUp() throws IOException {
		ChatModel geminiChatModel = GoogleAiGeminiChatModel.builder()
				.apiKey(System.getenv("GEMINI_API_KEY"))
				.modelName("gemini-2.5-flash")
				.build();
		analysis = new LangChain4jPropertiesAnalysis(geminiChatModel);
		pageContent = Jsoup.parse(new File("src/test/resources/fixtures/leilao-sample.html"), "UTF-8")
				.body().text();
	}

	@Test
	void extractsStructuredDataFromRealGeminiResponse() {
		PropertyAnalysisResult result = analysis.analyze(pageContent);

		assertThat(result.modalidade()).containsIgnoringCase("judicial");
		assertThat(result.dataLeilao()).contains("26/04/2024");
		assertThat(result.tipoImovel()).containsIgnoringCase("apartamento");
		assertThat(result.valorMercado()).isEqualByComparingTo(new BigDecimal("2451000"));
		assertThat(result.valorArrematacao()).isEqualByComparingTo(new BigDecimal("1488250"));
		assertThat(result.iptuAnual()).isEqualByComparingTo(new BigDecimal("4750"));
		assertThat(result.condominioMensal()).isNull();
	}

	@Test
	void typeReturnsLangChainIdentifier() {
		assertThat(analysis.type()).isEqualTo("lang_chain");
	}
}
