package br.com.ralvin.propertyanalysis.analysis;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiChatModelConfigTest {

	@Test
	void buildsChatModelWithoutStrictJsonSchemaCapability() {
		ChatModel chatModel = new GeminiChatModelConfig()
				.geminiChatModel("fake-key-for-test", "gemini-2.5-flash");

		assertThat(chatModel).isNotNull();
		assertThat(chatModel.supportedCapabilities()).doesNotContain(Capability.RESPONSE_FORMAT_JSON_SCHEMA);
	}
}
