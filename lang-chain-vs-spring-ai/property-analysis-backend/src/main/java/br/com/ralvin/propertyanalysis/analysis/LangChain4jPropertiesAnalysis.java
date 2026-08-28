package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.PropertyExtractionException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.stereotype.Component;

@Component
public class LangChain4jPropertiesAnalysis implements AIPropertiesAnalisys {

	private final PropertyExtractor extractor;

	public LangChain4jPropertiesAnalysis(ChatModel geminiChatModel) {
		this.extractor = AiServices.builder(PropertyExtractor.class)
				.chatModel(geminiChatModel)
				.build();
	}

	@Override
	public String type() {
		return "lang_chain";
	}

	@Override
	public PropertyAnalysisResult analyze(String pageContent) {
		try {
			return extractor.extract(pageContent);
		} catch (RuntimeException e) {
			throw new PropertyExtractionException("Failed to extract property data via LangChain4j", e);
		}
	}

	interface PropertyExtractor {

		@UserMessage("""
				Extraia os dados do leilão de imóvel a partir do conteúdo da página abaixo.
				Se um campo não estiver presente no conteúdo, retorne o literal JSON null para ele.
				Exemplo: se o condomínio mensal não for mencionado no texto, o campo
				"condominioMensal" do JSON de saída deve ser exatamente `null`, e nunca
				-1, 0, ou qualquer outro número de placeholder.

				Conteúdo da página:
				{{content}}
				""")
		PropertyAnalysisResult extract(@V("content") String content);
	}
}
