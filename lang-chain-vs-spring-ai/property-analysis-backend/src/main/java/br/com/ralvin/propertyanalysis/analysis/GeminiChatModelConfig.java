package br.com.ralvin.propertyanalysis.analysis;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiChatModelConfig {

	@Bean
	public ChatModel geminiChatModel(
			@Value("${gemini.api-key}") String apiKey,
			@Value("${gemini.model-name:gemini-2.5-flash}") String modelName) {
		return GoogleAiGeminiChatModel.builder()
				.apiKey(apiKey)
				.modelName(modelName)
				.build();
	}
}
