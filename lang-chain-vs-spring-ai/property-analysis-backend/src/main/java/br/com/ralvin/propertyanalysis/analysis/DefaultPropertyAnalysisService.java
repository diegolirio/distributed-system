package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.InvalidAnalysisLinkException;
import br.com.ralvin.propertyanalysis.analysis.exception.UnsupportedAnalysisTypeException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DefaultPropertyAnalysisService implements PropertyAnalysisService {

	private final PageFetcher pageFetcher;
	private final Map<String, AIPropertiesAnalisys> analyzersByType;

	public DefaultPropertyAnalysisService(PageFetcher pageFetcher, List<AIPropertiesAnalisys> analyzers) {
		this.pageFetcher = pageFetcher;
		this.analyzersByType = analyzers.stream()
				.collect(Collectors.toMap(AIPropertiesAnalisys::type, analyzer -> analyzer));
	}

	@Override
	public PropertyAnalysisResult analyze(String type, String link) {
		validateLink(link);
		AIPropertiesAnalisys analyzer = analyzersByType.get(type);
		if (analyzer == null) {
			throw new UnsupportedAnalysisTypeException("Unsupported analysis type: " + type);
		}
		String pageContent = pageFetcher.fetch(link);
		return analyzer.analyze(pageContent);
	}

	private void validateLink(String link) {
		URI uri;
		try {
			uri = new URI(link);
		} catch (URISyntaxException e) {
			throw new InvalidAnalysisLinkException("Invalid link: " + link);
		}
		String scheme = uri.getScheme();
		boolean validScheme = "http".equals(scheme) || "https".equals(scheme);
		if (!validScheme || uri.getHost() == null) {
			throw new InvalidAnalysisLinkException("Invalid link: " + link);
		}
	}
}
