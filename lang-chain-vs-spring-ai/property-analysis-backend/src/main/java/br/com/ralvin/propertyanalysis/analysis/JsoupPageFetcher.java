package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.PageFetchException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsoupPageFetcher implements PageFetcher {

	private static final String USER_AGENT =
			"Mozilla/5.0 (compatible; PropertyAnalysisBot/1.0; +https://github.com/diegolirio)";
	private static final int TIMEOUT_MILLIS = 10_000;

	@Override
	public String fetch(String url) {
		try {
			Document document = Jsoup.connect(url)
					.userAgent(USER_AGENT)
					.timeout(TIMEOUT_MILLIS)
					.get();
			return document.body().text();
		} catch (IOException e) {
			throw new PageFetchException("Failed to fetch auction page: " + url, e);
		}
	}
}
