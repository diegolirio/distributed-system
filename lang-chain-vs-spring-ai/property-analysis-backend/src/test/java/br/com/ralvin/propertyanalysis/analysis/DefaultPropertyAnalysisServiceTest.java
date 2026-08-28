package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.InvalidAnalysisLinkException;
import br.com.ralvin.propertyanalysis.analysis.exception.UnsupportedAnalysisTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPropertyAnalysisServiceTest {

	@Mock
	private PageFetcher pageFetcher;

	private DefaultPropertyAnalysisService service;
	private FakeAnalyzer fakeAnalyzer;

	@BeforeEach
	void setUp() {
		fakeAnalyzer = new FakeAnalyzer();
		service = new DefaultPropertyAnalysisService(pageFetcher, List.of(fakeAnalyzer));
	}

	@Test
	void delegatesToFetcherAndMatchingAnalyzer() {
		when(pageFetcher.fetch("https://example.com/leilao/1")).thenReturn("page text");

		PropertyAnalysisResult result = service.analyze("lang_chain", "https://example.com/leilao/1");

		assertThat(result).isSameAs(fakeAnalyzer.lastResult);
		assertThat(fakeAnalyzer.lastPageContent).isEqualTo("page text");
		verify(pageFetcher).fetch("https://example.com/leilao/1");
	}

	@Test
	void rejectsInvalidLink() {
		assertThatThrownBy(() -> service.analyze("lang_chain", "not-a-url"))
				.isInstanceOf(InvalidAnalysisLinkException.class);
	}

	@Test
	void rejectsUnsupportedType() {
		assertThatThrownBy(() -> service.analyze("spring_ai", "https://example.com/leilao/1"))
				.isInstanceOf(UnsupportedAnalysisTypeException.class);
	}

	private static class FakeAnalyzer implements AIPropertiesAnalisys {

		private String lastPageContent;
		private final PropertyAnalysisResult lastResult = new PropertyAnalysisResult(
				"Judicial", "2ª", "À vista", "26/04/2024", "residencial-apartamento",
				"Rua Exemplo, 123", null, null, null, null);

		@Override
		public String type() {
			return "lang_chain";
		}

		@Override
		public PropertyAnalysisResult analyze(String pageContent) {
			this.lastPageContent = pageContent;
			return lastResult;
		}
	}
}
