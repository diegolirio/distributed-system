package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.InvalidAnalysisLinkException;
import br.com.ralvin.propertyanalysis.analysis.exception.PageFetchException;
import br.com.ralvin.propertyanalysis.analysis.exception.PropertyExtractionException;
import br.com.ralvin.propertyanalysis.analysis.exception.UnsupportedAnalysisTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PropertyAnalysisService service;

	@Test
	void returnsAnalysisResultOnSuccess() throws Exception {
		PropertyAnalysisResult result = new PropertyAnalysisResult(
				"Judicial", "2ª", "À vista", "26/04/2024", "residencial-apartamento",
				"Rua Exemplo, 123", new BigDecimal("2451000"), new BigDecimal("1488250"),
				new BigDecimal("4750"), null);
		when(service.analyze(eq("lang_chain"), eq("https://example.com/leilao/1"))).thenReturn(result);

		mockMvc.perform(post("/analysis")
						.param("type", "lang_chain")
						.param("link", "https://example.com/leilao/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.modalidade").value("Judicial"))
				.andExpect(jsonPath("$.valorMercado").value(2451000));
	}

	@Test
	void returns400OnInvalidLink() throws Exception {
		when(service.analyze(eq("lang_chain"), eq("not-a-url")))
				.thenThrow(new InvalidAnalysisLinkException("Invalid link: not-a-url"));

		mockMvc.perform(post("/analysis")
						.param("type", "lang_chain")
						.param("link", "not-a-url"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid link: not-a-url"));
	}

	@Test
	void returns501OnUnsupportedType() throws Exception {
		when(service.analyze(eq("spring_ai"), eq("https://example.com/leilao/1")))
				.thenThrow(new UnsupportedAnalysisTypeException("Unsupported analysis type: spring_ai"));

		mockMvc.perform(post("/analysis")
						.param("type", "spring_ai")
						.param("link", "https://example.com/leilao/1"))
				.andExpect(status().isNotImplemented());
	}

	@Test
	void returns502OnPageFetchFailure() throws Exception {
		when(service.analyze(eq("lang_chain"), eq("https://example.com/leilao/1")))
				.thenThrow(new PageFetchException("boom", new java.io.IOException()));

		mockMvc.perform(post("/analysis")
						.param("type", "lang_chain")
						.param("link", "https://example.com/leilao/1"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.message").value("Não foi possível acessar a página do leilão."));
	}

	@Test
	void returns502OnPropertyExtractionFailure() throws Exception {
		when(service.analyze(eq("lang_chain"), eq("https://example.com/leilao/1")))
				.thenThrow(new PropertyExtractionException("boom", new RuntimeException()));

		mockMvc.perform(post("/analysis")
						.param("type", "lang_chain")
						.param("link", "https://example.com/leilao/1"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.message").value("Não foi possível extrair os dados do imóvel. Tente novamente."));
	}
}
